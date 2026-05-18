package order;

import java.math.BigDecimal;

public class DutyCheckResult {

    private boolean exceeded;
    private String message;
    private BigDecimal estimatedTax;

    public DutyCheckResult(
            boolean exceeded,
            String message,
            BigDecimal estimatedTax
    ) {
        this.exceeded = exceeded;
        this.message = message;
        this.estimatedTax = estimatedTax;
    }

    public boolean isExceeded() {
        return exceeded;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getEstimatedTax() {
        return estimatedTax;
    }
}