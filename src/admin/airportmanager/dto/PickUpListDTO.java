package admin.airportmanager.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PickUpListDTO {
    // 픽업 정보
    private int pickupId;
    private LocalDateTime pickupAvailableAt;  
    private LocalDateTime pickedUpAt;          

    // 주문 정보
    private int orderId;
    private String orderState;
    private LocalDateTime orderedAt;

    // 회원 정보
    private int memberId;
    private String memberName;
    private String passportNumber;
    private String grade;

    // 항공편 정보
    private String flightCode;
    private LocalDateTime departureAt;
}