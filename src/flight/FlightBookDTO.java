package flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FlightBookDTO {

    private int reservationId;
    private int memberId;
    private int flightId;
    private String reservationCode;
}