package member;

import java.time.LocalDate;
import java.time.Period;

import common.Grade;
import exception.ErrorCode;
import exception.ValidationException;
import membership.MembershipService;

public class MemberService {

    private final MemberDAO memberDAO = new MemberDAO();
    
    private final MembershipService membershipService = new MembershipService();
    
    public Member getMemberById(int memberId) {
        Member member = memberDAO.findById(memberId);

        if (member == null) {
            throw new ValidationException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return member;
    }
    // 회원가입
    public void signup(MemberSignupDTO dto) {

    	// 입력값이 비어있지 않은지 검증
        validateSignupInput(dto);

        if (memberDAO.existsByLoginId(dto.getLoginId())) {
            throw new ValidationException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (memberDAO.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new ValidationException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        Member member = Member.builder()
                .loginId(dto.getLoginId())
                .password(dto.getPassword())
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .phoneNumber(dto.getPhoneNumber())
                .grade(Grade.SILVER)
                .gradeSelectionDate(LocalDate.now())
                .isAdult(isAdult(dto.getBirthDate()))
                .build();

        memberDAO.insert(member);
    }

    // 회원가입 입력값 검증
    private void validateSignupInput(MemberSignupDTO dto) {

        if (dto == null) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        if (isBlank(dto.getLoginId())) {
            throw new ValidationException(ErrorCode.INVALID_LOGIN_ID);
        }

        if (isBlank(dto.getPassword())) {
            throw new ValidationException(ErrorCode.INVALID_PASSWORD);
        }

        if (isBlank(dto.getName())) {
            throw new ValidationException(ErrorCode.INVALID_MEMBER_NAME);
        }

        if (dto.getBirthDate() == null) {
            throw new ValidationException(ErrorCode.INVALID_BIRTH_DATE);
        }

        if (isBlank(dto.getPhoneNumber())) {
            throw new ValidationException(ErrorCode.INVALID_PHONE_NUMBER);
        }
    }

    // 생년월일 기준으로 성인 여부 판별
    private boolean isAdult(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 19;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    // 여권 정보 등록
    public void registerPassport(int memberId, String passportNum, LocalDate passportExpiredDate) {

        validatePassportInput(passportNum, passportExpiredDate);

        if (memberDAO.existsByPassportNum(passportNum)) {
            throw new ValidationException(ErrorCode.DUPLICATE_PASSPORT);
        }

        memberDAO.updatePassport(memberId, passportNum, passportExpiredDate);
    }

    // 여권번호 및 여권 만료일 검증
    private void validatePassportInput(String passportNum, LocalDate passportExpiredDate) {

        // 여권번호를 입력하지 않은 경우
        if (isBlank(passportNum)) {
            throw new ValidationException(ErrorCode.EMPTY_PASSPORT_NUMBER);
        }

        // 신여권 - 영문 1자 + 숫자 3자리 + 영문 1자 + 숫자 4자리
        // 구여권 - 영문 1자 + 숫자 8자리
        // 두 경우 모두 허용
        String passportPattern = "^[A-Z][0-9]{8}$|^[A-Z][0-9]{3}[A-Z][0-9]{4}$";

        // 여권번호 형식이 일치하지 않는 경우
        if (!passportNum.matches(passportPattern)) {
            throw new ValidationException(ErrorCode.INVALID_PASSPORT_FORMAT);
        }

        // 여권 만료일을 입력하지 않은 경우
        if (passportExpiredDate == null) {
            throw new ValidationException(ErrorCode.EMPTY_PASSPORT_EXPIRY_DATE);
        }

        // 여권이 이미 만료된 경우
        if (!passportExpiredDate.isAfter(LocalDate.now())) {
            throw new ValidationException(ErrorCode.EXPIRED_PASSPORT);
        }
    }
    
    // 로그인
    public int login(String loginId, String password) {

        // 아이디 또는 비밀번호를 입력하지 않은 경우
        if (isBlank(loginId) || isBlank(password)) {
            throw new ValidationException(ErrorCode.INVALID_LOGIN_INPUT);
        }

        Member member = memberDAO.findByLoginIdAndPassword(loginId, password);

        // 로그인 정보가 일치하지 않는 경우
        if (member == null) {
            throw new ValidationException(ErrorCode.INVALID_LOGIN_CREDENTIAL);
        }
        
        validatePassportNotExpired(member);

        membershipService.updateMembershipGradeIfExpired(member.getMemberId());

        return member.getMemberId();
    }
    
    // 여권 만료 검증
    private void validatePassportNotExpired(Member member) {

        LocalDate passportExpiredDate = member.getPassportExpiredDate();

        if (passportExpiredDate == null) {
            return;
        }

        // 만료됐을 경우 예외 발생
        if (!passportExpiredDate.isAfter(LocalDate.now())) {
            throw new ValidationException(ErrorCode.EXPIRED_PASSPORT);
        }
    }
}