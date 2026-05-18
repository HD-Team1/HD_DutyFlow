package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import airplane.Airplane;
import common.CurrentTime;
import exception.ErrorCode;
import exception.QueueException;
import main.Application;
import member.Member;

public class MLPQ {
	private PriorityQueue<PickUpTicket> aq;
	private PriorityQueue<PickUpTicket> bq;

	private int lastNum;
	private final int promotionThresholdMinutes;
	private final int maxWaitTimeMinutes;

	public MLPQ() {
		this.lastNum = 1;
		this.promotionThresholdMinutes = 30;
		this.maxWaitTimeMinutes = 40;
		// enqueue에서 NPE 터짐 방
		this.aq = new PriorityQueue<>();
		this.bq = new PriorityQueue<>();
	}

	// 1. 시간 경과 및 승격(Promote)만 처리하는 메서드
	public void passTime() {
		CurrentTime.curTime = CurrentTime.curTime.plusMinutes(1);
		this.promote();
	}

	// 2. 데이터 팝(Pop) 시점에 Starvation(에이징) 대상자를 최우선으로 가로채도록 수정
	public PickUpTicket pop() {
		// 0순위: 40분 이상 대기자(Starvation)가 있는지 먼저 확인하고 있으면 바로 꺼냄
		PickUpTicket starved = this.handleStarvation();
		if (starved != null)
			return starved;

		// 1, 2순위: AQ -> BQ 순서로 팝
		if (this.aq.size() > 0)
			return this.aq.poll();
		else if (this.bq.size() > 0)
			return this.bq.poll();
		else
			throw new QueueException(ErrorCode.QUEUE_EMPTY, new Exception("호출할 대기열이 비어있습니다."));
	}

	public void enqueue(PickUpTicket ticket) {
		if (Duration.between(CurrentTime.curTime, ticket.getAirplane().getDepartureAt()).getSeconds()
				/ 60 < this.promotionThresholdMinutes) {
			this.aq.add(ticket);
		} else {
			this.bq.add(ticket);
		}
	}
	
	public int nextNum() {
	    return ++this.lastNum;
	}

	public PickUpTicket peek() {
		if (this.aq.size() > 0)
			return this.aq.peek();
		else if (this.bq.size() > 0)
			return this.bq.peek();
		else
			throw new QueueException(ErrorCode.QUEUE_EMPTY, new Exception("조회할 대기열이 비어있습니다."));
	}

	private void moveToA(PickUpTicket p) {
		this.aq.add(this.bq.poll());
	}

	// 수정 예시: removeIf 활용 (Java 8 이상)
	private void promote() {
		// 출국시간이 30분 미만인 것들을 임시 리스트에 담고 aq에 넣은 뒤 bq에서 제거
		List<PickUpTicket> toPromote = this.bq.stream()
				.filter(p -> Duration.between(CurrentTime.curTime, p.getAirplane().getDepartureAt()).getSeconds()
						/ 60 < this.promotionThresholdMinutes)
				.collect(Collectors.toList()); // <--- 이 부분을 수정했습니다!

		this.aq.addAll(toPromote);
		this.bq.removeAll(toPromote);
	}

	private PickUpTicket handleStarvation() {
		for (PickUpTicket p : this.aq) {
			if (Duration.between(p.getTicketIssueTime(), CurrentTime.curTime).getSeconds()
					/ 60 >= this.maxWaitTimeMinutes) {
				PickUpTicket tempP = p;
				this.aq.remove(p);
				return tempP;
			}
		}
		for (PickUpTicket p : this.bq) {
			if (Duration.between(p.getTicketIssueTime(), CurrentTime.curTime).getSeconds()
					/ 60 >= this.maxWaitTimeMinutes) {
				PickUpTicket tempP = p;
				this.bq.remove(p);
				return tempP;
			}
		}
		return null;
	}

	/**
	 * 대기열에서 특정 티켓을 찾아 삭제합니다. (호출 후 미방문 고객 처리나 강제 제외 시 사용)
	 */
	public boolean removeTicket(PickUpTicket ticket) {
		if (ticket == null)
			return false;

		boolean removed = false;
		// 긴급 큐에서 찾아 삭제
		removed = this.aq.remove(ticket);

		// 긴급 큐에 없었다면 일반 큐에서 찾아 삭제
		if (!removed) {
			removed = this.bq.remove(ticket);
		}

		if (removed) {
			System.out.println("[MLPQ] 대기열에서 티켓 삭제 완료: " + ticket.getMember().getName());
		}
		return removed;
	}

	public void makeMLPQ(SortStrategy aqStrategy, SortStrategy bqStrategy) {
		this.aq = new PriorityQueue<PickUpTicket>(aqStrategy.getComparator());
		this.bq = new PriorityQueue<PickUpTicket>(bqStrategy.getComparator());
	}

	public int size() {
		return this.aq.size() + this.bq.size();
	}

	// aq 복사본
	public List<PickUpTicket> getAllFromAq() {
		return new ArrayList<>(this.aq);
	}

	// bq 복사본
	public List<PickUpTicket> getAllFromBq() {
		return new ArrayList<>(this.bq);
	}
	
	/**
	 * AQ·BQ에서 출국 시간이 경과한(departureAt < now) 티켓 목록을 반환합니다.
	 * 큐 자체를 변경하지 않으므로, 제거는 호출자가 removeExpiredTickets()로 별도 수행합니다.
	 */
	public List<PickUpTicket> getExpiredTickets(LocalDateTime now) {
		List<PickUpTicket> expired = new ArrayList<>();
 
		for (PickUpTicket ticket : this.aq) {
			if (now.isAfter(ticket.getAirplane().getDepartureAt())) {
				expired.add(ticket);
			}
		}
		for (PickUpTicket ticket : this.bq) {
			if (now.isAfter(ticket.getAirplane().getDepartureAt())) {
				expired.add(ticket);
			}
		}
 
		return expired;
	}
 
	/**
	 * 전달받은 티켓들을 AQ·BQ에서 일괄 제거합니다.
	 */
	public void removeExpiredTickets(List<PickUpTicket> tickets) {
		this.aq.removeAll(tickets);
		this.bq.removeAll(tickets);
	}

	public void clearAll() {
		this.aq.clear();
		this.bq.clear();
	}
}
