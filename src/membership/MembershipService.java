package membership;

import java.math.BigDecimal;
import java.time.LocalDate;

import common.Grade;
import exception.ErrorCode;
import exception.ValidationException;

public class MembershipService {
	private final MembershipDAO membershipDAO = new MembershipDAO();

    // 등급 재산정
    public void updateMembershipGrade(int memberId) {

    	// 최근 1년 간 구매 총액
        BigDecimal yearlyAmount = membershipDAO.sumPurchaseAmountLastOneYear(memberId);

        if (yearlyAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.INVALID_PURCHASE_AMOUNT);
        }

        Grade newGrade = Grade.fromPurchaseAmount(yearlyAmount);
        Grade currentGrade = membershipDAO.findGradeByMemberId(memberId);

        // 현재 회원 등급과 새로 계산한 등급이 같지 않으면 업데이트
        if (!newGrade.equals(currentGrade)) {
            membershipDAO.updateGrade(memberId, newGrade);
        }
    }
}