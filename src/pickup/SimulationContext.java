package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import airplane.Airplane;
import common.CurrentTime;
import common.Grade;
import member.Member;

public class SimulationContext {

	// ── 기준 시각 (시나리오 시작 시 복원) ──
	public static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 5, 1, 9, 30, 0);

	// ── 티켓 정보 ──
	public static class TicketInfo {
		public final Member member;
		public final Airplane airplane;
		public final LocalDateTime issueAt;
		public boolean issued = false;
		public PickUpTicket ticket;
		public String status = "WAITING"; // WAITING→PICKUP_RESERVED→CALLED→PICKED_UP/NO_SHOW/TIMEOUT

		public TicketInfo(Member m, Airplane a, LocalDateTime issueAt) {
			this.member = m;
			this.airplane = a;
			this.issueAt = issueAt;
		}
	}

	// ── 스냅샷 (언두용) ──
	private static class Snapshot {
		LocalDateTime time;
		int calledTIIdx; // -1 if none
		LocalDateTime callTime;
		boolean counterOpen, calledFromAq;
		boolean[] issued;
		String[] statuses;
		PickUpTicket[] tickets;
		LocalDateTime[] departures;
	}

	// ── 핵심 데이터 ──
	public MLPQ pq;
	private PickUpTicket currentTicket;
	private LocalDateTime callTime;
	private boolean isCounterOpen = false;
	private boolean calledFromAq = false;
	private boolean pickupInProgress = false;
	private static final int CALL_TIMEOUT_LIMIT = 10;

	// ── 시나리오 ──
	private List<TicketInfo> ticketInfos = new ArrayList<>();
	private final List<String> eventLog = new ArrayList<>();
	private final List<String> promotionAlerts = new ArrayList<>();
	private final Deque<Snapshot> undoStack = new ArrayDeque<>();

	public SimulationContext() {
		this.pq = new MLPQ();
		this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
	}

	/*
	 * ============================================================ 스냅샷 저장/복원
	 * ============================================================
	 */
	private Snapshot createSnapshot() {
		Snapshot s = new Snapshot();
		s.time = CurrentTime.curTime;
		s.calledTIIdx = findTIIndex(currentTicket);
		s.callTime = callTime;
		s.counterOpen = isCounterOpen;
		s.calledFromAq = calledFromAq;
		int n = ticketInfos.size();
		s.issued = new boolean[n];
		s.statuses = new String[n];
		s.tickets = new PickUpTicket[n];
		s.departures = new LocalDateTime[n];
		for (int i = 0; i < n; i++) {
			TicketInfo ti = ticketInfos.get(i);
			s.issued[i] = ti.issued;
			s.statuses[i] = ti.status;
			s.tickets[i] = ti.ticket;
			s.departures[i] = ti.airplane.getDepartureAt();
		}
		return s;
	}

	private void restoreSnapshot(Snapshot s) {
		CurrentTime.curTime = s.time;
		isCounterOpen = s.counterOpen;
		calledFromAq = s.calledFromAq;
		callTime = s.callTime;

		for (int i = 0; i < ticketInfos.size(); i++) {
			TicketInfo ti = ticketInfos.get(i);
			ti.issued = s.issued[i];
			ti.status = s.statuses[i];
			ti.ticket = s.tickets[i];
			ti.airplane.setDepartureAt(s.departures[i]);
		}

		currentTicket = (s.calledTIIdx >= 0) ? ticketInfos.get(s.calledTIIdx).ticket : null;

		// 큐 재구성
		pq.clearAll();
		for (int i = 0; i < ticketInfos.size(); i++) {
			TicketInfo ti = ticketInfos.get(i);
			if (ti.issued && ti.ticket != null && !"PICKED_UP".equals(ti.status) && !"NO_SHOW".equals(ti.status)
					&& !"TIMEOUT".equals(ti.status) && !"CALLED".equals(ti.status)) {
				pq.enqueue(ti.ticket);
			}
		}
	}

	public boolean canUndo() {
		return !undoStack.isEmpty();
	}

	public void undo() {
		if (undoStack.isEmpty())
			return;
		restoreSnapshot(undoStack.pop());
		log("⏪ " + CurrentTime.curTime.toLocalTime() + " 복원");
	}

	private int findTIIndex(PickUpTicket t) {
		if (t == null)
			return -1;
		for (int i = 0; i < ticketInfos.size(); i++)
			if (ticketInfos.get(i).ticket == t)
				return i;
		return -1;
	}

	/*
	 * ============================================================ 시간 경과
	 * ============================================================
	 */
	public void passTime() {
		undoStack.push(createSnapshot());
		if (undoStack.size() > 300)
			((ArrayDeque<Snapshot>) undoStack).removeLast();

		List<PickUpTicket> aqBefore = new ArrayList<>(pq.getAllFromAq());
		pq.passTime(); // +1min + promote

		// 프로모션 감지
		for (PickUpTicket t : pq.getAllFromAq()) {
			if (!aqBefore.contains(t)) {
				promotionAlerts.add(t.getMember().getName() + " 고객 출국 30분 미만 → AQ 승격!");
				log("⬆️ [승격] " + t.getMember().getName() + " BQ→AQ");
			}
		}
		issueScheduledTickets();

		if (!pickupInProgress) {
			updateNoShowState();
			checkCallingTimeout();
			tryCallNextCustomer();
		} else {
			updateNoShowStateQueueOnly();
		}
		updateNoShowStateOrdersOnly();
	}

	private void issueScheduledTickets() {
		for (TicketInfo ti : ticketInfos) {
			if (!ti.issued && !CurrentTime.curTime.isBefore(ti.issueAt)) {
				ti.ticket = new PickUpTicket(ti.member, ti.airplane, pq.nextNum(), 0);
				pq.enqueue(ti.ticket);
				ti.issued = true;
				ti.status = "PICKUP_RESERVED";
				log("🎫 [발권] " + ti.member.getName() + " (" + ti.member.getGrade().name() + ", "
						+ ti.airplane.getFlightCode() + ")");
			}
		}
	}

	/*
	 * ============================================================ 창구 조작
	 * ============================================================
	 */
	public void openCounter() {
		isCounterOpen = true;
		log("🏢 창구 오픈 (" + CurrentTime.curTime.toLocalTime() + ")");
		tryCallNextCustomer();
	}

	private void tryCallNextCustomer() {
		if (isCounterOpen && currentTicket == null && pq.size() > 0) {
			// 호출 전 AQ 스냅샷 → 어느 큐에서 나왔는지 판별
			List<PickUpTicket> aqBefore = new ArrayList<>(pq.getAllFromAq());
			currentTicket = pq.pop();
			calledFromAq = aqBefore.contains(currentTicket);
			callTime = CurrentTime.curTime;
			TicketInfo ti = findTI(currentTicket);
			if (ti != null)
				ti.status = "CALLED";
			log("📢 [호출] " + currentTicket.getMember().getName() + (calledFromAq ? " (AQ)" : " (BQ)"));
		}
	}

	public PickUpTicket getCurrentTicket() {
		return currentTicket;
	}

	public LocalDateTime getCallTime() {
		return callTime;
	}

	public boolean isCalledFromAq() {
		return calledFromAq;
	}

	public boolean isCounterOpen() {
		return isCounterOpen;
	}

	public void clearCurrentTicket() {
		currentTicket = null;
		callTime = null;
	}

	/*
	 * ============================================================ 물품 인도
	 * ============================================================
	 */
	public void setPickupInProgress(boolean v) {
		this.pickupInProgress = v;
	}

	public boolean isPickupInProgress() {
		return pickupInProgress;
	}

	public void processPickUp() {
		if (currentTicket == null) {
			log("⚠️ 호출된 고객 없음");
			return;
		}
		TicketInfo ti = findTI(currentTicket);
		if (ti != null)
			ti.status = "PICKED_UP";
		log("✅ [수령] " + currentTicket.getMember().getName() + " 인도 완료 (" + CurrentTime.curTime.toLocalTime() + ")");
		currentTicket = null;
		callTime = null;
		pickupInProgress = false;
		tryCallNextCustomer();
	}

	/*
	 * ============================================================ 노쇼
	 * ============================================================
	 */
	private void updateNoShowState() {
		if (currentTicket != null && CurrentTime.curTime.isAfter(currentTicket.getAirplane().getDepartureAt())) {
			doNoShow(currentTicket);
			currentTicket = null;
			callTime = null;
		}
		processQueueNoShows();
	}

	private void updateNoShowStateQueueOnly() {
		processQueueNoShows();
	}

	private void updateNoShowStateOrdersOnly() {
		processOrdersNoShows();
	}

	private void processQueueNoShows() {
		List<PickUpTicket> expired = pq.getExpiredTickets(CurrentTime.curTime);
		for (PickUpTicket t : expired)
			doNoShow(t);
		if (!expired.isEmpty())
			pq.removeExpiredTickets(expired);
	}

	private void processOrdersNoShows() {
		for (TicketInfo ti : ticketInfos) {
			if ("TIMEOUT".equals(ti.status) && CurrentTime.curTime.isAfter(ti.airplane.getDepartureAt())) {
				ti.status = "NO_SHOW";
				log("🛫 [NO_SHOW] " + ti.member.getName() + " (" + ti.airplane.getFlightCode() + ")");
			}
		}
	}

	public void doNoShow(PickUpTicket ticket) {
		TicketInfo ti = findTI(ticket);
		if (ti != null)
			ti.status = "NO_SHOW";
		log("🛫 [NO_SHOW] " + ticket.getMember().getName() + " (" + ticket.getAirplane().getFlightCode() + ")");
	}

	/*
	 * ============================================================ 호출 타임아웃
	 * ============================================================
	 */
	private void checkCallingTimeout() {
		if (currentTicket == null || callTime == null)
			return;
		long waited = Duration.between(callTime, CurrentTime.curTime).toMinutes();
		int timeout = CALL_TIMEOUT_LIMIT;
		if (pq.size() > 0) {
			long ml = Duration.between(CurrentTime.curTime, pq.peek().getAirplane().getDepartureAt()).toMinutes();
			if (ml < (CALL_TIMEOUT_LIMIT * 1.5))
				timeout = (int) (CALL_TIMEOUT_LIMIT * 0.5);
		}
		if (waited >= timeout) {
			TicketInfo ti = findTI(currentTicket);
			if (ti != null)
				ti.status = "TIMEOUT";
			log("⏰ [타임아웃] " + currentTicket.getMember().getName() + " (" + waited + "/" + timeout + "분)"
					+ (timeout < CALL_TIMEOUT_LIMIT ? " — 골든타임 보호 발동!" : ""));
			pq.removeTicket(currentTicket);
			currentTicket = null;
			callTime = null;
		}
	}

	/*
	 * ============================================================ 항공 지연
	 * ============================================================
	 */
	public void delayFlight(String code, LocalDateTime newTime) {
		boolean found = false;
		for (PickUpTicket t : pq.getAllFromAq())
			if (t.getAirplane().getFlightCode().equals(code)) {
				t.getAirplane().setDepartureAt(newTime);
				found = true;
				break;
			}
		if (!found)
			for (PickUpTicket t : pq.getAllFromBq())
				if (t.getAirplane().getFlightCode().equals(code)) {
					t.getAirplane().setDepartureAt(newTime);
					found = true;
					break;
				}
		if (!found && currentTicket != null && currentTicket.getAirplane().getFlightCode().equals(code)) {
			currentTicket.getAirplane().setDepartureAt(newTime);
			found = true;
		}
		if (found)
			log("✈️ [지연] " + code + " → " + newTime.toLocalTime());
	}

	public void rescheduledPq() {
		if (pq.size() == 0)
			return;
		List<PickUpTicket> all = new ArrayList<>();
		all.addAll(pq.getAllFromAq());
		all.addAll(pq.getAllFromBq());
		pq.clearAll();
		for (PickUpTicket t : all)
			pq.enqueue(t);
		log("🔄 재정렬 완료 (" + pq.size() + "명)");
	}

	/*
	 * ============================================================ 조회
	 * ============================================================
	 */
	public List<TicketInfo> getTicketInfos() {
		return ticketInfos;
	}

	public List<TicketInfo> getActiveTicketInfos() {
		return ticketInfos.stream().filter(ti -> ti.issued).filter(
				ti -> !"PICKED_UP".equals(ti.status) && !"NO_SHOW".equals(ti.status) && !"TIMEOUT".equals(ti.status))
				.collect(Collectors.toList());
	}

	private TicketInfo findTI(PickUpTicket t) {
		if (t == null)
			return null;
		for (TicketInfo ti : ticketInfos)
			if (ti.ticket == t)
				return ti;
		return null;
	}

	private void log(String m) {
		eventLog.add(m);
	}

	public List<String> drainLog() {
		List<String> c = new ArrayList<>(eventLog);
		eventLog.clear();
		return c;
	}

	public List<String> drainPromotionAlerts() {
		List<String> c = new ArrayList<>(promotionAlerts);
		promotionAlerts.clear();
		return c;
	}

	/*
	 * ============================================================ 시나리오 로딩
	 * ============================================================
	 */
	public void loadScenario(int num) {
		resetAll();
		CurrentTime.curTime = BASE_TIME;
		switch (num) {
		case 1:
			load1();
			break;
		case 2:
			load2();
			break;
		case 3:
			load3();
			break;
		case 4:
			load4();
			break;
		case 5:
			load5();
			break;
		case 6:
			load6();
			break;
		case 7:
			load7();
			break;
		}
	}

	public void resetAll() {
		pq.clearAll();
		ticketInfos.clear();
		undoStack.clear();
		currentTicket = null;
		callTime = null;
		isCounterOpen = false;
		calledFromAq = false;
		pickupInProgress = false;
		eventLog.clear();
		promotionAlerts.clear();
	}

	private void add(String name, String pp, Grade g, String fl, LocalDateTime dep, LocalDateTime issue) {
		int id = ticketInfos.size() + 1;
		ticketInfos.add(new TicketInfo(new Member(id, name, pp, true, g), new Airplane(id, fl, dep), issue));
	}

	/* ── 시나리오 1: 정상 호출 흐름 ── */
	private void load1() {
		LocalDateTime n = BASE_TIME;
		add("이급박", "M11", Grade.GOLD, "KE305", n.plusMinutes(25), n.plusMinutes(1));
		add("오블랙", "M44", Grade.BLACK, "OZ102", n.plusMinutes(45), n.plusMinutes(2));
		add("최골드", "M55", Grade.GOLD, "KE081", n.plusMinutes(60), n.plusMinutes(3));
		add("강부자", "M99", Grade.PRESTIGE, "OZ773", n.plusMinutes(90), n.plusMinutes(4));
		add("유실버", "M66", Grade.SILVER, "OZ541", n.plusMinutes(120), n.plusMinutes(5));
		add("김철용", "M33", Grade.SILVER, "KE651", n.plusMinutes(100), n.plusMinutes(6));
		isCounterOpen = true;
	}

	/* ── 시나리오 2: 호출 타임아웃 ── */
	private void load2() {
		LocalDateTime n = BASE_TIME;
		add("강부자", "M99", Grade.PRESTIGE, "OZ773", n.plusMinutes(90), n.plusMinutes(1));
		add("오블랙", "M44", Grade.BLACK, "KE081", n.plusMinutes(60), n.plusMinutes(2));
		add("최골드", "M55", Grade.GOLD, "KE305", n.plusMinutes(50), n.plusMinutes(3));
		add("박지각", "M22", Grade.SILVER, "OZ102", n.plusMinutes(40), n.plusMinutes(4));
		add("유실버", "M66", Grade.SILVER, "OZ541", n.plusMinutes(120), n.plusMinutes(5));
		isCounterOpen = true;
	}

	/* ── 시나리오 3: 노쇼 자동 처리 ── */
	private void load3() {
		LocalDateTime n = BASE_TIME;
		add("박지각", "M22", Grade.SILVER, "KE999", n.plusMinutes(8), n.plusMinutes(1));
		add("이급박", "M11", Grade.GOLD, "OZ888", n.plusMinutes(12), n.plusMinutes(1));
		add("최골드", "M55", Grade.GOLD, "KE081", n.plusMinutes(60), n.plusMinutes(2));
		add("강부자", "M99", Grade.PRESTIGE, "OZ773", n.plusMinutes(90), n.plusMinutes(3));
		add("유실버", "M66", Grade.SILVER, "OZ541", n.plusMinutes(120), n.plusMinutes(4));
		isCounterOpen = true;
	}

	/* ── 시나리오 4: 우선 등급 입장 ── */
	private void load4() {
		LocalDateTime n = BASE_TIME;
		add("김철용", "M33", Grade.SILVER, "KE651", n.plusMinutes(100), n.plusMinutes(1));
		add("최골드", "M55", Grade.GOLD, "KE081", n.plusMinutes(60), n.plusMinutes(2));
		add("유실버", "M66", Grade.SILVER, "OZ541", n.plusMinutes(120), n.plusMinutes(3));
		add("약블랙", "M77", Grade.BLACK, "KE305", n.plusMinutes(80), n.plusMinutes(4));
		add("강부자", "M99", Grade.PRESTIGE, "OZ773", n.plusMinutes(90), n.plusMinutes(9));
		isCounterOpen = true;
	}

	/* ── 시나리오 5: Starvation 방지 (에이징) ── */
	private void load5() {
		LocalDateTime n = BASE_TIME;

		// 09:30에 선행 서비스가 시작되고, 김철용은 09:31에 발권된 뒤
		// 이후 13명의 BLACK / PRESTIGE 고객이 계속 발권되어 BQ에 오래 머무르게 한다.
		add("김부자", "M71", Grade.PRESTIGE, "OZ901", n.plusMinutes(50), n.plusMinutes(1));
		add("김철용", "M33", Grade.SILVER, "KE651", n.plusMinutes(100), n.plusMinutes(2));
		add("이부자", "M72", Grade.BLACK, "KE902", n.plusMinutes(95), n.plusMinutes(3));
		add("박부자", "M73", Grade.PRESTIGE, "OZ903", n.plusMinutes(98), n.plusMinutes(4));
		add("최부자", "M74", Grade.BLACK, "KE904", n.plusMinutes(102), n.plusMinutes(5));
		add("정부자", "M75", Grade.PRESTIGE, "OZ905", n.plusMinutes(106), n.plusMinutes(6));
		add("강부자", "M76", Grade.BLACK, "KE906", n.plusMinutes(110), n.plusMinutes(7));
		add("한부자", "M77", Grade.PRESTIGE, "OZ907", n.plusMinutes(114), n.plusMinutes(8));
		add("송부자", "M78", Grade.BLACK, "KE908", n.plusMinutes(118), n.plusMinutes(9));
		add("윤부자", "M79", Grade.PRESTIGE, "OZ909", n.plusMinutes(122), n.plusMinutes(10));
		add("오부자", "M80", Grade.BLACK, "KE910", n.plusMinutes(126), n.plusMinutes(11));
		add("문부자", "M81", Grade.PRESTIGE, "OZ911", n.plusMinutes(130), n.plusMinutes(12));
		add("임부자", "M82", Grade.BLACK, "KE912", n.plusMinutes(134), n.plusMinutes(13));
		add("서부자", "M83", Grade.PRESTIGE, "OZ913", n.plusMinutes(138), n.plusMinutes(14));
		add("조부자", "M84", Grade.BLACK, "KE914", n.plusMinutes(142), n.plusMinutes(15));
		add("왕부자", "M85", Grade.PRESTIGE, "KE915", n.plusMinutes(146), n.plusMinutes(16));
		isCounterOpen = true;
	}

	/* ── 시나리오 6: 항공 지연 → 재정렬 ── */
	private void load6() {
		LocalDateTime n = BASE_TIME;
		add("김지각", "M10", Grade.SILVER, "KE301", n.plusMinutes(20), n.plusMinutes(1));
		add("이급박", "M11", Grade.GOLD, "KE305", n.plusMinutes(25), n.plusMinutes(2));
		add("오블랙", "M44", Grade.BLACK, "OZ102", n.plusMinutes(45), n.plusMinutes(3));
		add("최골드", "M55", Grade.GOLD, "KE081", n.plusMinutes(60), n.plusMinutes(4));
		add("강부자", "M99", Grade.PRESTIGE, "OZ773", n.plusMinutes(90), n.plusMinutes(5));
		add("유실버", "M66", Grade.SILVER, "OZ541", n.plusMinutes(120), n.plusMinutes(6));
		isCounterOpen = true;
	}

	/* ── 시나리오 7: 골든타임 보호 (동적 타임아웃) ── */
	private void load7() {
		LocalDateTime n = BASE_TIME;
		// 구민 dep 09:43 (AQ), 이급박 dep 09:47 (AQ, 다음 대기자)
		// 09:33에 이급박 출국 14분 < 15분 → 골든타임 발동 → 타임아웃 5분
		// 09:36에 구민 5분 대기 → 타임아웃 (일반이면 09:41에 10분)
		add("구민", "M00", Grade.GOLD, "KE081", n.plusMinutes(13), n.plusMinutes(1));
		add("이급박", "M11", Grade.GOLD, "KE305", n.plusMinutes(17), n.plusMinutes(2));
		add("오블랙", "M44", Grade.BLACK, "OZ102", n.plusMinutes(60), n.plusMinutes(3));
		add("강부자", "M99", Grade.PRESTIGE, "OZ773", n.plusMinutes(90), n.plusMinutes(4));
		add("최골드", "M55", Grade.GOLD, "OZ541", n.plusMinutes(100), n.plusMinutes(5));
		isCounterOpen = true;
	}

	/*
	 * ============================================================ 시나리오 설명
	 * ============================================================
	 */
	public static String getDescription(int num) {
		switch (num) {
		case 1:
			return "【정상 호출 흐름】\n" + "권장 인도시간: 3분\n\n" + "[멤버]\n" + " 이급박(GOLD, KE305) 출국 09:55 → AQ\n"
					+ " 오블랙(BLACK, OZ102) 출국 10:15 → BQ\n" + " 최골드(GOLD, KE081) 출국 10:30 → BQ\n"
					+ " 강부자(PRESTIGE, OZ773) 출국 11:00 → BQ\n" + " 유실버(SILVER, OZ541) 출국 11:30 → BQ\n"
					+ " 김철용(SILVER, KE651) 출국 11:10 → BQ\n\n" + "[진행]\n" + "1) → 6회: 번호표 순차 발권\n"
					+ "   이급박→AQ, 나머지→BQ\n" + "2) 이급박 자동 호출 → 수령 클릭 (3분)\n" + "3) 다음: 강부자(PRESTIGE) BQ 1순위\n"
					+ "4) 강부자→오블랙→최골드→김철용→유실버\n\n" + "[관찰] AQ 우선 → BQ 등급순 처리";

		case 2:
			return "【호출 타임아웃】\n" + "권장 인도시간: 3분\n\n" + "[멤버]\n" + " 강부자(PRESTIGE) 출국 11:00 → BQ 1순위\n"
					+ " 오블랙(BLACK) 출국 10:30 → BQ\n" + " 최골드(GOLD) 출국 10:20 → BQ\n" + " 박지각(SILVER) 출국 10:10 → BQ\n"
					+ " 유실버(SILVER) 출국 11:30 → BQ\n\n" + "[진행]\n" + "1) → 5회: 전원 발권\n" + "2) 강부자 자동 호출\n"
					+ "3) 수령 버튼 누르지 않고 → 계속 전진\n" + "4) 10분 후 → 타임아웃 발생!\n" + "5) 강부자 큐+타임라인에서 제거\n"
					+ "6) 다음 호출: 오블랙\n\n" + "[관찰] 10분 미방문 → 자동 타임아웃";

		case 3:
			return "【노쇼 자동 처리】\n" + "권장 인도시간: 3분\n\n" + "[멤버]\n" + " 박지각(SILVER, KE999) 출국 09:38 → AQ\n"
					+ " 이급박(GOLD, OZ888) 출국 09:42 → AQ\n" + " 최골드(GOLD) 출국 10:30 → BQ\n"
					+ " 강부자(PRESTIGE) 출국 11:00 → BQ\n" + " 유실버(SILVER) 출국 11:30 → BQ\n\n" + "[진행]\n"
					+ "1) → 키로 발권 후 계속 전진\n" + "2) 박지각 호출 → 수령 안 함\n" + "3) 09:38 경과 → 박지각 NO_SHOW\n"
					+ "4) 이급박 호출 → 수령 안 함\n" + "5) 09:42 경과 → 이급박 NO_SHOW\n" + "6) 주문섹션에서 NO_SHOW 표시 확인\n\n"
					+ "[관찰] 출국시간 경과 → 자동 NO_SHOW";

		case 4:
			return "【우선 등급 입장】\n" + "권장 인도시간: 3분\n\n" + "[멤버] (발권 순서)\n" + " 김철용(SILVER) → BQ 하위\n"
					+ " 최골드(GOLD) → BQ 중위\n" + " 유실버(SILVER) → BQ 하위\n" + " 약블랙(BLACK) → BQ 상위\n"
					+ " 강부자(PRESTIGE) ← 9분 후 합류!\n\n" + "[진행]\n" + "1) → 4회: 4명 발권\n" + "   BQ: 약블랙>최골드>김철용>유실버\n"
					+ "2) → 5회 더: 강부자(PRESTIGE) 발권\n" + "3) 강부자 → BQ 최상위로 등극!\n\n" + "[관찰] PRESTIGE가 늦게 와도 BQ 1순위";

		case 5:
			return "【Starvation 방지 (에이징)】\n" + "권장 인도시간: 3분\n\n" + "[멤버]\n" + " 김부자1(PRESTIGE) 09:30 발권 → 선행 서비스 시작\n"
					+ " 김철용(SILVER) 09:31 발권 → BQ 대기 진입\n" + " 이부자2(BLACK) 09:32 발권\n" + " 박부자3(PRESTIGE) 09:33 발권\n"
					+ " 최부자4(BLACK) 09:34 발권\n" + " 정부자5(PRESTIGE) 09:35 발권\n" + " 강부자6(BLACK) 09:36 발권\n"
					+ " 한부자7(PRESTIGE) 09:37 발권\n" + " 송부자8(BLACK) 09:38 발권\n" + " 윤부자9(PRESTIGE) 09:39 발권\n"
					+ " 오부자10(BLACK) 09:40 발권\n" + " 문부자11(PRESTIGE) 09:41 발권\n" + " 임부자12(BLACK) 09:42 발권\n"
					+ " 서부자13(PRESTIGE) 09:43 발권\n" + " 조부자14(BLACK) 09:44 발권\n\n" + "[진행]\n"
					+ "1) 09:30 김부자1 선행 호출 및 인도 시작\n" + "2) 09:31 김철용 발권 후 BQ 대기 유지\n"
					+ "3) 이후 09:32~09:44 사이 BLACK / PRESTIGE 고객 13명 추가 발권\n" + "   → 매번 BQ 우선 호출 / 인도 반복\n"
					+ "4) 김철용은 계속 BQ에 남아 40분 이상 대기\n" + "5) 10:11경: 김철용 대기 40분 도달!\n" + "6) 다음 pop() 시 김철용이 최우선 호출\n"
					+ "   (SILVER임에도 에이징으로 1순위!)\n\n" + "[관찰] 장기 대기 고객이 BQ 하위에 묶여 있다가\n"
					+ "       40분 기준 도달 시 등급 무시 최우선 호출";
		case 6:
			return "【항공 지연 → 재정렬】\n" + "권장 인도시간: 3분\n\n" + "[멤버]\n" + " 이급박(GOLD, KE305) 출국 09:55 → AQ\n"
					+ " 오블랙(BLACK) 출국 10:15 → BQ\n" + " 최골드(GOLD) 출국 10:30 → BQ\n" + " 강부자(PRESTIGE) 출국 11:00 → BQ\n"
					+ " 유실버(SILVER) 출국 11:30 → BQ\n\n" + "[진행]\n" + "1) → 5회: 전원 발권\n"
					+ "   AQ: 이급박 | BQ: 강부자>오블랙>최골드>유실버\n" + "2) 시나리오 6 버튼 재클릭!\n" + "   → KE305 3시간 지연 (09:55→12:55)\n"
					+ "3) 이급박 AQ→BQ 강등 확인\n" + "   BQ: 강부자>오블랙>최골드>이급박>유실버\n\n" + "[관찰] 항공 지연 시 AQ↔BQ 재분류";

		case 7:
			return "【골든타임 보호 (동적 타임아웃)】\n" + "권장 인도시간: 3분\n\n" + "[멤버]\n" + " 구민(GOLD, KE081) 출국 09:43 → AQ\n"
					+ " 이급박(GOLD, KE305) 출국 09:47 → AQ\n" + " 오블랙(BLACK) 출국 10:30 → BQ\n"
					+ " 강부자(PRESTIGE) 출국 11:00 → BQ\n" + " 최골드(GOLD) 출국 11:10 → BQ\n\n" + "[진행]\n" + "1) → 5회: 전원 발권\n"
					+ "   구민 AQ 호출 (출국 가장 빠름)\n" + "2) 수령 안 함 → 계속 전진\n" + "3) 09:33 (→ 3회): 이급박 출국까지 14분\n"
					+ "   < 15분 감지 → 타임아웃 5분 단축!\n" + "4) 09:36 (→ 6회): 구민 5분 대기\n" + "   → 골든타임 타임아웃 발동!\n"
					+ "   (일반이면 09:41에 10분 타임아웃)\n\n" + "[관찰] 다음 대기자 출국 임박 시\n" + "       타임아웃 10분→5분 자동 단축\n"
					+ "       → 5분 더 빠르게 다음 고객 서비스";

		default:
			return "";
		}
	}
}