package member;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberSignupDTO {

    private final String loginId;
    private final String password;
    private final String name;
    private final LocalDate birthDate;
    private final String phoneNumber;
    
}