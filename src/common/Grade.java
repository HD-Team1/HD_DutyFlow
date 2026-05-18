package common;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Grade {
	PRESTIGE(
		    1,
		    new BigDecimal("4000"),
		    new BigDecimal("20")
	),
	BLACK(
	    2,
	    new BigDecimal("2000"),
	    new BigDecimal("15")
	),
	GOLD(
	    3,
	    new BigDecimal("800"),
	    new BigDecimal("10")
	),
	SILVER(
	    4,
	    BigDecimal.ZERO,
	    new BigDecimal("5")
	);
	
    private final int priority;

    public static Grade fromPriority(int priority) {

        for (Grade grade : values()) {
            if (grade.priority == priority) {
                return grade;
            }
        }
		return null;
    }
    
    // 등급 선정 기준 금액
    private final BigDecimal criteriaAmount;

    // 할인율
    private final BigDecimal discountRate;

    // 구매 총액을 기준으로 등급 반환
    public static Grade fromPurchaseAmount(BigDecimal amount) {
        if (amount == null) {
            return SILVER;
        }

        if (amount.compareTo(PRESTIGE.criteriaAmount) >= 0) {
            return PRESTIGE;
        }

        if (amount.compareTo(BLACK.criteriaAmount) >= 0) {
            return BLACK;
        }

        if (amount.compareTo(GOLD.criteriaAmount) >= 0) {
            return GOLD;
        }

        return SILVER;
    }
}