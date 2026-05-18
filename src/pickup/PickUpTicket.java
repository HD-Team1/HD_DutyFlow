package pickup;

import java.time.LocalDateTime;

import airplane.Airplane;
import common.CurrentTime;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import member.Member;

@Getter
@Setter
public class PickUpTicket{
	private Member member;
	private Airplane airplane;
	private int reservationId;
	private LocalDateTime ticketIssueTime;
	private int ticketNum;
	
	public PickUpTicket(Member member, Airplane airplane, int ticketNum, int reservationId) {  // ← 파라미터 추가
		this.member=member;
		this.airplane=airplane;
		this.ticketNum=ticketNum;
		this.ticketIssueTime = CurrentTime.curTime;
		this.reservationId = reservationId;  // ← 추가
	}
}
