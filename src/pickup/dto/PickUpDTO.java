package pickup.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PickUpDTO {
	/*
	 * 필요 데이터:
	 * 픽업.픽업가능일시, 회원.여권번호, 예약.예약번호, 비행기.출발시간
	 * 
	 * 테이블 조인:
	 * 픽업&주문&회원&예약&비행기
	 */
	
	private LocalDateTime pickupAvailableAt;
	private String passportNumber;
	private String reservationCode;
	private LocalDateTime departureAt;
}
