package airplane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import flight.FlightObserver;
import lombok.Data;
import lombok.Getter;

@Getter
public class Airplane {
	private int flightId;
	private String flightCode;
	private LocalDateTime departureAt;
	private List<FlightObserver> observers;
	private int isDelayed; // 0: 정상, 1: 지연
	
	public Airplane(int flightId, String flightCode, LocalDateTime departureAt) {
        this.flightId = flightId;
        this.flightCode = flightCode;
        this.departureAt = departureAt;
        this.observers = new ArrayList<>();
        this.isDelayed = 0;
    }
	
	//Observer 등록
	public void registerObserver(FlightObserver observer) {
		if(!observers.contains(observer)) {
			observers.add(observer);
		}
	}
	
	//Observer 해제
	public void removeObserver(FlightObserver observer) {
		if(observers.contains(observer)) {
			observers.remove(observer);
		} else {
			System.out.println("[Airplane] 등록되지 않은 옵저버");
		}
	}
	
	//등록된 옵저버에게 지연 이벤트 처리
	private void notifyObserver() {
		for(FlightObserver observer : observers) {
			observer.onFlightDelayReceived(this); // 변경된 Airplane 정보 전달 
		}
	}
	
	// 지연 시각 업데이트 
	public void setDepartureAt(LocalDateTime newDepartureAt) {
		
		if (newDepartureAt == null) {
			System.out.println("[Airplane] 유효하지 않은 출발 시각");
            return;
        }
		
		LocalDateTime newTime = newDepartureAt;
		
		if(newTime.isBefore(this.departureAt)) {
			System.out.println("[Airplane] 출국 시간보다 이른 지연시각");
			return;
		}
		
		if(newTime.isEqual(this.departureAt)) {
			System.out.println("[Airplane] 시간 변경없음");
			return;
		}
		
		if(newTime.isAfter(this.departureAt)) {
			System.out.println("FlightCode : " + flightCode + " || 지연 : " +  this.departureAt + " -> "  + newTime);
			
			this.departureAt = newTime;
			this.isDelayed = 1;
			notifyObserver();
		}
		
	}
	
	
	
	
	
	
}
