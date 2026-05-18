package exchangeRate.test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import exchangeRate.ExchangeRate;
import exchangeRate.ExchangeRateApiClient;
import exchangeRate.ExchangeRateDAO;
import exchangeRate.ExchangeRateProvider;
import product.Product;
import product.ProductDAO;
import product.dto.ProductDTO;

// 환율 자동 갱신을 테스트하기 위해 갱신 주기를 10초로 설정한 테스트 클래스
public class ExchangeRateTenSecondTest {

    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ExchangeRateApiClient exchangeRateApiClient = new ExchangeRateApiClient();

    // 테스트 초기 데이터가 TRUNC(SYSDATE - 7)이므로 7일 전 날짜를 기준으로 시작
    // 외부 API에서 실제 정보를 가져오므로 임의로 7일 전 날짜부터 가져오도록 설정했음
    private final LocalDate baseDate = LocalDate.now().minusDays(7);
    private int dayOffset = 0;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("===== 환율 10초 갱신 테스트 시작 =====");

        ExchangeRateTenSecondTest test = new ExchangeRateTenSecondTest();

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        // 10초마다 환율 자동 갱신 테스트 실행
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // exchangeDate가 PK이므로 테스트 날짜를 하루씩 증가시킴
                LocalDate testDate =
                        test.baseDate.plusDays(++test.dayOffset);

                test.updateExchangeRateForTest(testDate);

            } catch (Exception e) {
                System.out.println("[환율 10초 테스트 오류] " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);

        Thread.sleep(40000);

        scheduler.shutdown();

        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            scheduler.shutdownNow();
        }

        System.out.println("===== 10초 환율 갱신 테스트 종료 =====");
    }

    // 테스트용 환율 갱신 메서드
    private void updateExchangeRateForTest(LocalDate exchangeDate) {
        Connection conn = null;

        try {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);

            BigDecimal newExchangeRate =
                    exchangeRateApiClient.fetchUsdKrwRate(exchangeDate)
                            .setScale(4, RoundingMode.HALF_UP);

            exchangeRateDAO.updateLatestToN(conn);
            insertRate(conn, exchangeDate, newExchangeRate);
            productDAO.updateAllPriceKrw(conn, newExchangeRate);

            conn.commit();

            ExchangeRateProvider.getInstance()
                    .update(newExchangeRate, exchangeDate);

            Product product =
                    productDAO.getProductsByProductName("샤넬 향수");

            BigDecimal todayRate =
                    exchangeRateDAO.findTodayRate(conn);

            List<ExchangeRate> weeklyRates =
                    exchangeRateDAO.findWeeklyRates(conn);

            List<ExchangeRate> monthlyRates =
                    exchangeRateDAO.findMonthlyRates(conn);

            System.out.println("[" + exchangeDate + "] 환율 갱신 완료");
            System.out.println("현재 Provider 환율: "
                    + ExchangeRateProvider.getInstance().getExchangeRate());
            System.out.println("현재 Provider 날짜: "
                    + ExchangeRateProvider.getInstance().getExchangeDate());

            System.out.println("오늘 환율 조회: " + todayRate);
            System.out.println("최근 일주일 환율 조회: " + weeklyRates);
            System.out.println("최근 한 달 환율 조회: " + monthlyRates);

            System.out.println("상품명: " + product.getProductName());
            System.out.println("상품 달러 가격: " + product.getPriceUsd());
            System.out.println("상품 원화 가격: " + product.getPriceKrw());
            System.out.println("================================");

        } catch (Exception e) {
            rollback(conn);
            throw new SystemException(ErrorCode.DB_CONNECTION, e);

        } finally {
            close(conn);
        }
    }

    // 테스트 날짜 기준 환율 저장
    private void insertRate(Connection conn,
                            LocalDate exchangeDate,
                            BigDecimal exchangeRate) {

        String sql =
                "INSERT INTO ExchangeRate(exchangeDate, exchangeRate, isLatest) "
                        + "VALUES (?, ?, 'Y')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(exchangeDate));
            pstmt.setBigDecimal(2, exchangeRate);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    private void rollback(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    private void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}