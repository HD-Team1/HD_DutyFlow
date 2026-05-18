package pickup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import admin.airportmanager.AirportManagerDao;
import admin.airportmanager.AirportManagerService;
import admin.airportmanager.dto.PickUpListDTO;
import airplane.Airplane;
import common.CurrentTime;
import exception.BusinessException;
import exception.DataNotFoundException;
import exception.DutyFreeException;
import exception.ErrorCode;
import exception.ValidationException;
import flight.FlightDAO;
import flight.FlightObserver;
import flight.FlightService;
import member.Member;
import order.Order;
import order.OrderDAO;
import order.dto.OrderUpdateDTO;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;

public class PickUpSystem implements FlightObserver {

	public MLPQ pq;
	private final PickUpDAO pickUpDAO = new PickUpDAO();
	private final AirportManagerService airportManagerService;
	private final OrderDAO orderDAO = new OrderDAO();
	private final FlightDAO flightDAO = new FlightDAO();
	private List<Order> orders; // loadOrders()를 통해 채워질 주문 목록
	private PickUpTicket currentTicket;
	private LocalDateTime callTime; // 💡 고객을 호출한 시각 기록
	private static final int CALL_TIMEOUT_LIMIT = 10; // 기본 타임아웃 (10분)
	private boolean isCounterOpen = false; // 창구 오픈 상태

	private final FlightService flightService;

	public PickUpSystem(AirportManagerService airportManagerService, FlightService flightService) {
		this.pq = new MLPQ();
		this.pq.makeMLPQ(new DepartureSoonSortStrategy(), new PrioritySortStrategy());
		this.airportManagerService = airportManagerService;
		this.flightService = flightService;
	}

	// 이제 가상 시계(pickedUpAt)를 함께 받습니다.
	public void updateOrderState(OrderUpdateDTO oud, LocalDateTime pickedUpAt) {
		this.pickUpDAO.updateOrderAndPickupState(oud.getOrderId(), oud.getNewState(), pickedUpAt);
	}

	// ---------------------------------------------------------
	// [호출] 다음 대기자를 부르고 시스템에 기억시킴
	// ---------------------------------------------------------
	// 💡 1. 창구 오픈 메서드 (업무 시작)
	public void openCounter() {
		this.isCounterOpen = true;
		System.out.println("\n🏢 [시스템] " + CurrentTime.curTime.toLocalTime() + ", 인도장 창구 업무가 시작되었습니다.");
		this.tryCallNextCustomer();
	}

	// 💡 2. 밖에서 현재 호출된 사람을 확인할 수 있게 getter 추가
	public PickUpTicket getCurrentTicket() {
		return this.currentTicket;
	}

	// 💡 3. 자동 호출 감지기 (오픈되어 있을 때만 부름!)
	private void tryCallNextCustomer() {
		if (this.isCounterOpen && this.currentTicket == null && this.pq.size() > 0) {
			// 큐에서 꺼내어 현재 호출자로 설정
			this.currentTicket = this.pq.pop();
			this.callTime = CurrentTime.curTime; // 💡 호출 시점 시각 저장
			System.out
					.println("\n📢 [시스템 자동 호출] 띵동~ [" + this.currentTicket.getMember().getName() + "] 고객님, 창구로 와주세요!");
		}
	}

	// 실제 물품 인도 프로세스
	public void processPickUp(String passportNum, int flightResNum, int processingTime) {
		if (this.currentTicket == null) {
			throw new BusinessException(ErrorCode.ILLEGAL_STATE, new Exception("현재 호출된 고객이 없습니다."));
		}

		System.out.println("▶ 1. 창구 방문 고객 확인: 여권[" + passportNum + "]");
		if (!this.currentTicket.getMember().getPassportNum().equals(passportNum)) {
			throw new ValidationException(ErrorCode.INVALID_INPUT,
					new Exception("호출된 대상[" + currentTicket.getMember().getName() + "]과 방문 고객 정보가 일치하지 않습니다."));
		}
		this.callTime = null;
		System.out.println("▶ 2. 본인 확인 완료! [" + currentTicket.getMember().getName() + "] 고객님 물품 인도를 시작합니다.");

		this.validateInfo(passportNum, flightResNum);

		System.out.println("▶ 3. 물품 확인 및 인도 중... (소요 예정: " + processingTime + "분)");
		for (int i = 0; i < processingTime; i++) {
			this.passTime(); // 처리 시간 흐름 (여기서 다른 고객들 승격 여부 판별)
		}

		int targetOrderId = this.pickUpDAO.getOrderIdForPickup(passportNum, flightResNum);
		this.pickUpDAO.updateOrderAndPickupState(targetOrderId, "PICKED_UP", CurrentTime.curTime);

		System.out.println("✔️ 4. [" + currentTicket.getMember().getName() + "]님 인도 완료. (시각: "
				+ CurrentTime.curTime.toLocalTime() + ")");
		System.out.println("-------------------------------------------------");

		// 🚨 업무 종료 -> 옵저버 해제 -> 창구 비움 -> 다음 사람 자동 호출!
		this.currentTicket.getAirplane().removeObserver(this);
		this.currentTicket = null;

		this.tryCallNextCustomer();
	}

	// ---------------------------------------------------------
	// [NO_SHOW] 출국 시간이 경과한 티켓을 자동으로 NO_SHOW 처리
	// ---------------------------------------------------------
	private void updateNoShowState() {
		// 1. 현재 호출 중인 currentTicket 검사
		if (this.currentTicket != null) {
			LocalDateTime departure = this.currentTicket.getAirplane().getDepartureAt();
			if (CurrentTime.curTime.isAfter(departure)) {
				processNoShow(this.currentTicket);
				this.currentTicket = null;
				this.callTime = null;
			}
		}

		// 2. 큐(AQ + BQ) 내 출국 경과 티켓 일괄 추출 및 제거
		List<PickUpTicket> expiredTickets = this.pq.getExpiredTickets(CurrentTime.curTime);

		for (PickUpTicket ticket : expiredTickets) {
			processNoShow(ticket);
		}

		if (!expiredTickets.isEmpty()) {
			this.pq.removeExpiredTickets(expiredTickets);
		}
	}

	private void processNoShow(PickUpTicket ticket) {
		String name = ticket.getMember().getName();
		String passportNum = ticket.getMember().getPassportNum();

		try {
			// 옵저버 해제
			ticket.getAirplane().removeObserver(this);

			// orderId 조회 (여권번호 + 예약번호 기반)
			// ※ PickUpTicket에는 reservationId가 없으므로 PickUpDAO를 통해 조회
			int orderId = this.pickUpDAO.getOrderIdForPickup(passportNum, ticket.getReservationId());

			Order order = this.orderDAO.findOneOrderByOrderId(orderId);
			if (order == null) {
				System.out.println("   ⚠️ [NO_SHOW] " + name + " — 주문 조회 실패, 건너뜀");
				return;
			}

			// PICKUP_RESERVED 상태일 때만 전이 (그 외 상태에서 noShow() 호출 시 예외 발생 방지)
			if (!"PICKUP_RESERVED".equals(order.getStateName())) {
				System.out.println("   ⚠️ [NO_SHOW] " + name + " — 상태가 " + order.getStateName() + "이므로 건너뜀");
				return;
			}

			order.noShow();
			this.pickUpDAO.updateOrderAndPickupState(orderId, "NO_SHOW", null);
			System.out.println("   🛫 [NO_SHOW] " + name + " 고객님 — 출국 시간 경과로 미수령(NO_SHOW) 처리 완료");

		} catch (DataNotFoundException e) {
			System.out.println("   ⚠️ [NO_SHOW] " + name + " — 주문 정보 조회 실패: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("   ⚠️ [NO_SHOW] " + name + " — 처리 중 예외 발생: " + e.getMessage());
		}
	}

	public void passTime() {
		this.pq.passTime();
		System.out.println("   (⏳ " + CurrentTime.curTime.toLocalTime() + " 경과...)");
		// 🛫 출국 시간 경과 티켓 → NO_SHOW 자동 전이
		updateNoShowState();
		// 🚨 실시간 호출 타임아웃 & 골든타임 감시
		checkCallingTimeout();

		this.tryCallNextCustomer();
	}

	// 💡 [핵심] 호출 타임아웃 검사 및 동적 조절
	private void checkCallingTimeout() {
		if (this.currentTicket != null && this.callTime != null) {
			long waitedMinutes = java.time.Duration.between(this.callTime, CurrentTime.curTime).toMinutes();

			// 기본 타임아웃 10분 설정
			int dynamicTimeout = CALL_TIMEOUT_LIMIT;

			// 💡 1. 큐의 다음 대기자를 peek() 하여 출국 임박 여부 검사
			if (this.pq.size() > 0) {
				PickUpTicket nextTicket = this.pq.peek();
				long minsLeft = java.time.Duration
						.between(CurrentTime.curTime, nextTicket.getAirplane().getDepartureAt()).toMinutes();

				// 💡 2. 다음 대기자의 출국 시간이 15분(10 * 1.5) 미만으로 남았다면!
				if (minsLeft < (CALL_TIMEOUT_LIMIT * 1.5)) {
					// 타임아웃을 5분(10 * 0.5)으로 반토막 냅니다.
					dynamicTimeout = (int) (CALL_TIMEOUT_LIMIT * 0.5);
				}
			}

			// 💡 3. 계산된 타임아웃과 현재 대기 시간 비교
			if (waitedMinutes >= dynamicTimeout) {
				System.out.println("\n⏰ [호출 타임아웃] " + currentTicket.getMember().getName() + " 고객님 미방문으로 호출을 취소합니다.");

				if (dynamicTimeout < CALL_TIMEOUT_LIMIT) {
					System.out.println("   (🚨 사유: 다음 대기자의 출국 임박으로 인한 골든타임 보호 조치 발동!)");
				}

				// 관찰자 해제 및 큐에서 완전 삭제
				this.currentTicket.getAirplane().removeObserver(this);
				this.pq.removeTicket(this.currentTicket);

				// 현재 호출 상태 비움 -> 즉시 다음 사람 자동 호출됨
				this.currentTicket = null;
				this.callTime = null;

				System.out.println("   (💡 환불되지 않으며, 해당 고객은 나중에 다시 번호표를 뽑아야 합니다.)");
			}
		}
	}

	// 큐에 번호표 뽑기 (뽑았는데 창구가 비어있으면 즉시 호출됨!)
	public void appendQueue(String passportNum, int flightResNum) throws DutyFreeException {
		this.validateInfo(passportNum, flightResNum);
		AppendQueueDTO aqdto = this.pickUpDAO.getAppendingInfo(passportNum, flightResNum);

		// 예약 유효성 검증
		flightService.getBookByMemberAndFlight(aqdto.getMemberId(), aqdto.getFlightCode());

		Airplane airplane = flightService.getFlightInfo(aqdto.getReservationCode());

		airplane.registerObserver(this);

		Member member = new Member(aqdto.getMemberId(), aqdto.getName(), passportNum, false, aqdto.getGrade());
		PickUpTicket ticket = new PickUpTicket(member, airplane, this.pq.nextNum(), flightResNum);

		this.pq.enqueue(ticket);
		this.tryCallNextCustomer();
	}

	// DB에서 조건에 맞는 주문들을 메모리로 로드
	public void loadOrders() {
		System.out.println("SYSTEM: 인도장 시스템에 픽업 대기 중인 주문 목록을 로드합니다...");
//		this.orders = this.orderDAO.getPendingOrders(CurrentTime.curTime);
		System.out.println("SYSTEM: 로드 완료 (총 " + this.orders.size() + "건의 대기 주문)");
	}

	private List<PickUpDTO> validateInfo(String passportNum, int flightResNum) {
		// DB 조회
		List<PickUpDTO> realPickUpList = this.pickUpDAO.getAllPickUp(passportNum, flightResNum);

		LocalDateTime pickUpAvailableAt = realPickUpList.get(0).getPickupAvailableAt();
		LocalDateTime departureAt = realPickUpList.get(0).getDepartureAt();

		// 검증 1: 아직 픽업 가능 시간이 안 된 경우
		if (CurrentTime.curTime.isBefore(pickUpAvailableAt)) {
			throw new ValidationException(ErrorCode.ILLEGAL_STATE, new Exception("아직 픽업 가능 시간이 아닙니다."));
		}
		// 검증 2: 이미 비행기가 떠난 경우 (No-Show)
		if (CurrentTime.curTime.isAfter(departureAt)) {
			// TODO: 노쇼처리 -> 자동환불
			throw new BusinessException(ErrorCode.NO_SHOW, new Exception("출국 시간이 경과하여 인도받을 수 없습니다."));
		}

		return realPickUpList;
	}

	public PickUpTicket popQueue() {
		return this.pq.pop(); // 내부에서 QueueException 발생 가능
	}

	// Observer 콜백 (이미 갱신된 departureAt Airplane 이 들어옴)
	@Override
	public void onFlightDelayReceived(Airplane airplane) {
		System.out.println("[PickUpSystem] 지연 이벤트 수신" + " | flightCode " + airplane.getFlightCode() + " | 지연 : "
				+ airplane.getDepartureAt());

		rescheduledPq();
	}

	public void rescheduledPq() {
		if (pq.size() == 0) {
			System.out.println("[PickUpSystem] 재정렬할 대기열 없음");
			return;
		}

		List<PickUpTicket> allTickets = new ArrayList<>();
		allTickets.addAll(pq.getAllFromAq());
		allTickets.addAll(pq.getAllFromBq());

		pq.clearAll();

		// 🔧 기존 티켓 객체를 그대로 재삽입 → ticketIssueTime, ticketNum 보존
		for (PickUpTicket ticket : allTickets) {
			pq.enqueue(ticket);
		}

		System.out.println("[PickUpSystem] 재정렬 완료 || 현재 대기 수: " + pq.size());
		printCurrentQueue();
	}

	public void printCurrentQueue() {

		if (pq.size() == 0) {
			System.out.println("[PickUpSystem] 대기열 없음");
			return;
		}

		System.out.println("===== aq (긴급 큐) =====");

		int rank = 1;
		for (PickUpTicket ticket : pq.getAllFromAq()) {

			System.out.println("[" + rank++ + "] " + ticket.getMember() + " | " + ticket.getAirplane().getFlightCode()
					+ " | 출국: " + ticket.getAirplane().getDepartureAt());
		}

		System.out.println("===== bq (일반 큐) =====");

		rank = 1;

		for (PickUpTicket ticket : pq.getAllFromBq()) {

			System.out.println("[" + rank++ + "] " + ticket.getMember() + " | " + ticket.getAirplane().getFlightCode()
					+ " | 출국: " + ticket.getAirplane().getDepartureAt());
		}
	}

	// flightCode로 PQ안 Airplane 찾아서 지연 처리하기 위함
	public void delayFlight(String flightCode, LocalDateTime newDepartureAt) {

		// ✅ 1. DB Flight 테이블도 함께 갱신 (핵심 추가분)
		flightDAO.updateDelayedFlight(flightCode, newDepartureAt);
		System.out.println("[PickUpSystem] DB Flight 테이블 갱신 완료: " + flightCode + " → " + newDepartureAt);

		// 2. 인메모리 Airplane 객체 갱신 (기존 로직 — Observer 트리거)
		// aq
		for (PickUpTicket ticket : pq.getAllFromAq()) {
			if (ticket.getAirplane().getFlightCode().equals(flightCode)) {
				ticket.getAirplane().setDepartureAt(newDepartureAt);
				return;
			}
		}

		// bq
		for (PickUpTicket ticket : pq.getAllFromBq()) {
			if (ticket.getAirplane().getFlightCode().equals(flightCode)) {
				ticket.getAirplane().setDepartureAt(newDepartureAt);
				return;
			}
		}

		// currentTicket도 확인 (호출 중인 고객의 항공편이 지연될 수 있음)
		if (currentTicket != null && currentTicket.getAirplane().getFlightCode().equals(flightCode)) {
			currentTicket.getAirplane().setDepartureAt(newDepartureAt);
			System.out.println("[PickUpSystem] 현재 호출 중인 고객의 항공편도 지연 반영");
			return;
		}

		System.out.println("[PickUpSystem] 해당 항공편 없음");
	}

	// 로그인
	public void login(int managerId, String password) {
        this.airportManagerService.login(managerId, password);
    }

	// 로그아웃
	public void logout() {
		this.airportManagerService.logout();
    }

	// 전체 픽업 목록
	public List<PickUpListDTO> getAllPickUpList() {
		return this.airportManagerService.getAllPickUpList();
	}

	// 특정 회원 픽업 목록
	public List<PickUpListDTO> getAllPickUpListByMember(Member member) {
		return this.airportManagerService.getAllPickUpListByMember(member);
    }

	// 기간별 픽업 목록
	public List<PickUpListDTO> getAllPickUpListByDateRange(LocalDate start, LocalDate end) {
		return this.airportManagerService.getAllPickUpListByDateRange(start, end);
    }
}
