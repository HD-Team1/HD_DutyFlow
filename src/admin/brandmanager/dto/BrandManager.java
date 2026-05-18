package admin.brandmanager.dto;

import admin.manager.Manager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class BrandManager extends Manager {

    private int annualLeaveCount;
    private String brandName;

    public BrandManager(
            int managerId,
            String managerName,
            String managerType,
            String password,
            int annualLeaveCount,
            String brandName
    ) {
        super(managerId, managerName, managerType, password);
        this.annualLeaveCount = annualLeaveCount;
        this.brandName = brandName;
    }

    @Override
    public boolean authenticate(String inputPassword) {
        return getPassword() != null && getPassword().equals(inputPassword);
    }
}