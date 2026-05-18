package payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentDTO {
    private final int orderId;
    private final BigDecimal amount;
    private final String cardNumber;

    // 개인정보 보호를 위해 카드번호 마스킹 처리
    public String getCardNumberMask() {
        String onlyDigits = cardNumber.replaceAll("[^0-9]", "");

        if (onlyDigits.length() < 4) {
            return "****";
        }

        return "****-****-****-" + onlyDigits.substring(onlyDigits.length() - 4);
    }
}