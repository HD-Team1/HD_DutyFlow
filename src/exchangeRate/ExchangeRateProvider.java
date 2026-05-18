package exchangeRate;

import java.math.BigDecimal;
import java.time.LocalDate;

// 현재 환율 정보를 전역으로 공유하기 위한 싱글톤 객체
public class ExchangeRateProvider {

	// 싱글톤 객체 생성
	// 딱 하나의 객체를 만들어서 공유함. 프로그램 내에서 new 불가
    private static final ExchangeRateProvider INSTANCE = new ExchangeRateProvider();

    private BigDecimal exchangeRate;
    private LocalDate exchangeDate;

    // 생성자가 private으로 정의됐으므로 외부에서 new 불가. 싱글톤을 강제함
    private ExchangeRateProvider() {}

    // 싱글톤 객체 반환. 항상 동일한 객체를 반환함
    public static ExchangeRateProvider getInstance() {
        return INSTANCE;
    }

    // 현재 환율 정보 갱신
    public synchronized void update(BigDecimal exchangeRate,
                                    LocalDate exchangeDate) {

        this.exchangeRate = exchangeRate;
        this.exchangeDate = exchangeDate;
    }

    // 현재 환율 반환
    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    // 환율 날짜 반환
    public LocalDate getExchangeDate() {
        return exchangeDate;
    }
}