package flight;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FlightDTO {

    private int flightId;
    private String flightCode;
    private LocalDateTime departureAt;
    private int isDelayed; // 0 : 정상, 1 : 지연
}