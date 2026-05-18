package admin.airportmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportManagerLoginDto {
	private int managerId;
	
	private String managerName; 
    private String managerType;  
}
