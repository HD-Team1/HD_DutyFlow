package pickup;

import java.util.Comparator;

public class PrioritySortStrategy implements SortStrategy {

    @Override
    public Comparator<PickUpTicket> getComparator() {

        return Comparator
                .comparingInt((PickUpTicket ticket) ->
                        ticket.getMember()
                              .getGrade()
                              .getPriority()
                )
                .thenComparingInt(PickUpTicket::getTicketNum);
    }
}
