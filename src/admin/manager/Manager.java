package admin.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Manager {

    private int managerId;
    private String managerName;
    private String managerType; // "BRAND", "AIRPORT", "SHOP"
    private String password;

    public abstract boolean authenticate(String inputPassword);
}