package flight;

import airplane.Airplane;

public interface FlightObserver {
	
	void onFlightDelayReceived(Airplane airplane);

}
