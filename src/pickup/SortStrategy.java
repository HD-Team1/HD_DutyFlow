package pickup;

import java.util.Comparator;

public interface SortStrategy {
	Comparator<PickUpTicket> getComparator();
}
