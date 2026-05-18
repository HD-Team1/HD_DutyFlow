package admin.airportmanager;

import java.time.LocalDateTime;

import admin.manager.Manager;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class AirportManager extends Manager {

    private LocalDateTime shiftTime;

    public AirportManager() {
        super();
    }

    public AirportManager(int managerId, String managerName, String managerType,
                          String password, LocalDateTime shiftTime) {
        super(managerId, managerName, managerType, password);
        this.shiftTime = shiftTime;
    }

    @Override
    public boolean authenticate(String inputPassword) {
    	return inputPassword != null && 
    	           getPassword() != null && 
    	           getPassword().equals(inputPassword);
    }
}