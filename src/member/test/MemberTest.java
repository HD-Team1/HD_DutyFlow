package member.test;

import java.time.LocalDate;

import member.MemberService;
import member.MemberSignupDTO;

public class MemberTest {

    public static void main(String[] args) {

        MemberService memberService = new MemberService();

        System.out.println("===== 회원가입 테스트 시작 =====");
        System.out.println();

        // =========================
        // 1. 정상 회원가입
        // =========================
        System.out.println("===== 정상 회원가입 =====");

        try {

            MemberSignupDTO dto = new MemberSignupDTO(
                    "testuser01",
                    "1234",
                    "김민준",
                    LocalDate.of(1998, 5, 10),
                    "01012345678"
            );

            memberService.signup(dto);

            System.out.println("회원가입 성공");

        } catch (Exception e) {
            System.out.println("회원가입 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 2. 예외 테스트 - 중복 아이디
        // =========================
        System.out.println("===== 예외 테스트 - 중복 아이디 =====");

        try {

            MemberSignupDTO dto = new MemberSignupDTO(
                    "testuser01",
                    "1234",
                    "홍길동",
                    LocalDate.of(1995, 1, 1),
                    "01099999999"
            );

            memberService.signup(dto);

            System.out.println("중복 아이디 회원가입 성공");

        } catch (Exception e) {
            System.out.println("중복 아이디 회원가입 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 3. 예외 테스트 - 중복 전화번호
        // =========================
        System.out.println("===== 예외 테스트 - 중복 전화번호 =====");

        try {

            MemberSignupDTO dto = new MemberSignupDTO(
                    "testuser02",
                    "1234",
                    "이수연",
                    LocalDate.of(1997, 3, 15),
                    "01012345678"
            );

            memberService.signup(dto);

            System.out.println("중복 전화번호 회원가입 성공");

        } catch (Exception e) {
            System.out.println("중복 전화번호 회원가입 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 4. 예외 테스트 - 아이디 공백
        // =========================
        System.out.println("===== 예외 테스트 - 아이디 공백 =====");

        try {

            MemberSignupDTO dto = new MemberSignupDTO(
                    " ",
                    "1234",
                    "김민준",
                    LocalDate.of(1998, 5, 10),
                    "01077777777"
            );

            memberService.signup(dto);

            System.out.println("공백 아이디 회원가입 성공");

        } catch (Exception e) {
            System.out.println("공백 아이디 회원가입 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 5. 예외 테스트 - 전화번호 공백
        // =========================
        System.out.println("===== 예외 테스트 - 전화번호 공백 =====");

        try {

            MemberSignupDTO dto = new MemberSignupDTO(
                    "testuser03",
                    "1234",
                    "김민준",
                    LocalDate.of(1998, 5, 10),
                    " "
            );

            memberService.signup(dto);

            System.out.println("공백 전화번호 회원가입 성공");

        } catch (Exception e) {
            System.out.println("공백 전화번호 회원가입 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 6. 정상 여권 등록
        // =========================
        System.out.println("===== 정상 여권 등록 =====");

        try {

            memberService.registerPassport(
                    1,
                    "M123A4567",
                    LocalDate.of(2030, 12, 31)
            );

            System.out.println("여권 등록 성공");

        } catch (Exception e) {
            System.out.println("여권 등록 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 7. 예외 테스트 - 여권번호 공백
        // =========================
        System.out.println("===== 예외 테스트 - 여권번호 공백 =====");

        try {

            memberService.registerPassport(
                    1,
                    " ",
                    LocalDate.of(2030, 12, 31)
            );

            System.out.println("공백 여권번호 등록 성공");

        } catch (Exception e) {
            System.out.println("공백 여권번호 등록 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 8. 예외 테스트 - 여권번호 형식 오류
        // =========================
        System.out.println("===== 예외 테스트 - 여권번호 형식 오류 =====");

        try {

            memberService.registerPassport(
                    1,
                    "123456789",
                    LocalDate.of(2030, 12, 31)
            );

            System.out.println("잘못된 여권번호 등록 성공");

        } catch (Exception e) {
            System.out.println("잘못된 여권번호 등록 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 9. 예외 테스트 - 만료된 여권
        // =========================
        System.out.println("===== 예외 테스트 - 만료된 여권 =====");

        try {

            memberService.registerPassport(
                    1,
                    "M12345678",
                    LocalDate.of(2020, 1, 1)
            );

            System.out.println("만료 여권 등록 성공");

        } catch (Exception e) {
            System.out.println("만료 여권 등록 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 10. 정상 로그인
        // =========================
        System.out.println("===== 정상 로그인 =====");

        try {

            int memberId = memberService.login(
                    "testuser01",
                    "1234"
            );

            System.out.println("로그인 성공 | memberId = " + memberId);

        } catch (Exception e) {
            System.out.println("로그인 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        // =========================
        // 11. 예외 테스트 - 로그인 실패
        // =========================
        System.out.println("===== 예외 테스트 - 로그인 실패 =====");

        try {

            memberService.login(
                    "testuser01",
                    "9999"
            );

            System.out.println("로그인 성공");

        } catch (Exception e) {
            System.out.println("로그인 실패 | reason = " + e.getMessage());
        }

        System.out.println();

        System.out.println("===== 회원 테스트 종료 =====");
        
     // =========================
     // 12. 예외 테스트 - 만료된 여권 로그인
     // =========================
     System.out.println("===== 예외 테스트 - 만료된 여권 로그인 =====");

     try {

         // 만료 여권 등록
         memberService.registerPassport(
                 1,
                 "M987A1234",
                 LocalDate.of(2020, 1, 1)
         );

         // 로그인 시도
         memberService.login(
                 "testuser01",
                 "1234"
         );

         System.out.println("만료 여권 로그인 성공");

     } catch (Exception e) {
         System.out.println("만료 여권 로그인 실패 | reason = " + e.getMessage());
     }

     System.out.println();
     
	  // =========================
	  // 12. 예외 테스트 - 만료된 여권 로그인
	  // =========================
	  System.out.println("===== 예외 테스트 - 만료된 여권 로그인 =====");
	
	  try {
	
	      // 만료 여권 등록
	      memberService.registerPassport(
	              1,
	              "M987A1234",
	              LocalDate.of(2020, 1, 1)
	      );
	
	      // 로그인 시도
	      memberService.login(
	              "testuser01",
	              "1234"
	      );
	
	      System.out.println("만료 여권 로그인 성공");
	
	  } catch (Exception e) {
	      System.out.println("만료 여권 로그인 실패 | reason = " + e.getMessage());
	  }
	
	  System.out.println();
	  
	// =========================
	// 12. 예외 테스트 - 만료된 여권 로그인
	// =========================
	System.out.println("===== 예외 테스트 - 만료된 여권 로그인 =====");

	try {

	    // 만료 여권 등록
	    memberService.registerPassport(
	            1,
	            "M987A1234",
	            LocalDate.of(2020, 1, 1)
	    );

	    // 로그인 시도
	    memberService.login(
	            "testuser01",
	            "1234"
	    );

	    System.out.println("만료 여권 로그인 성공");

	} catch (Exception e) {
	    System.out.println("만료 여권 로그인 실패 | reason = " + e.getMessage());
	}

	System.out.println();
    }
}