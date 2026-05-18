package pickup;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

import common.CurrentTime;
import main.Application;

public class DepartureSoonSortStrategy implements SortStrategy { 

	@Override
    public Comparator<PickUpTicket> getComparator() {

        return Comparator
                .comparingLong((PickUpTicket ticket) -> {

                    LocalDateTime departureAt =
                            ticket.getAirplane()
                                  .getDepartureAt();

                    return Duration.between(
                            CurrentTime.curTime,
                            departureAt
                    ).toMinutes();
                })
                .thenComparingInt((PickUpTicket ticket) ->
                				ticket.getTicketNum())
                .thenComparingInt(ticket ->
                        ticket.getMember()
                              .getGrade()
                              .getPriority()
                );
    }
	
}
