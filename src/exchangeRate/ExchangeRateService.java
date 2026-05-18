package exchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import product.ProductDAO;

public class ExchangeRateService {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE = BigDecimal.valueOf(1300);

    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();

    private final ProductDAO productDAO = new ProductDAO();
    
    private final ExchangeRateApiClient exchangeRateApiClient = new ExchangeRateApiClient();
    
    // 프로그램 시작 시 최신 환율 정보를 Provider에 초기화
    public void initializeExchangeRate() {
        try (Connection conn = OracleConnection.getConnection()) {

            BigDecimal todayRate = exchangeRateDAO.findTodayRate(conn); // 최신 환율

            // DB에 오늘 환율이 있으면 해당 값을 Provider에 초기화
            if (todayRate != null) {
                ExchangeRateProvider.getInstance().update(todayRate, LocalDate.now());
                return;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        // DB에 오늘 환율이 없으면 외부 API로 오늘 환율 갱신
        updateDailyExchangeRate();
    }

    // 매일 자정에 환율 업데이트
    public void updateDailyExchangeRate() {
        Connection conn = null;

        try {
            conn = OracleConnection.getConnection();
            
            // 원래 JDBC는 SQL 실행마다 자동 commit하지만,
            // 환율 업데이트와 상품 가격 수정을 모두 성공해야 커밋되도록 AutoCommit 설정을 끔
            // 환율 업데이트와 상품 가격 갱신을 하나의 트랜잭션으로 처리
            conn.setAutoCommit(false);

            // DB에서 최신 환율을 가져옴
            BigDecimal latestRate = exchangeRateDAO.findLatestRate(conn);

            if (latestRate == null) {
                latestRate = DEFAULT_EXCHANGE_RATE;
            }

            BigDecimal newExchangeRate = exchangeRateApiClient.fetchUsdKrwRate();
            newExchangeRate = newExchangeRate.setScale(4,RoundingMode.HALF_UP);

            // 기존 최신 환율은 flag값을 N으로 변경
            exchangeRateDAO.updateLatestToN(conn);
            // 오늘 날짜에 새로운 환율 저장
            exchangeRateDAO.insertTodayRate(conn, newExchangeRate);
            
            // 각 상품에 등록된 원화 가격 갱신
            productDAO.updateAllPriceKrw(conn, newExchangeRate);

            // 전체 작업 성공 시에 커밋함
            conn.commit();

            ExchangeRateProvider.getInstance()
                    .update(newExchangeRate, LocalDate.now());

        } catch (SystemException e) {
            rollback(conn);
            throw e;

        } catch (Exception e) {
            rollback(conn); // 오류 발생 시 롤백
            throw new SystemException(ErrorCode.DB_CONNECTION, e);

        } finally {
            close(conn);
        }
    }

    // 최신 환율 조회
    public BigDecimal getCurrentExchangeRate() {
        BigDecimal exchangeRate =
                ExchangeRateProvider.getInstance().getExchangeRate();

        if (exchangeRate == null) {
            initializeExchangeRate();
            exchangeRate = ExchangeRateProvider.getInstance().getExchangeRate();
        }

        return exchangeRate;
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
    
    // 오늘 환율 조회
    public BigDecimal getTodayExchangeRate() {
        try (Connection conn = OracleConnection.getConnection()) {
            return exchangeRateDAO.findTodayRate(conn);

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 최근 일주일 환율 조회
    public List<ExchangeRate> getWeeklyExchangeRates() {
        try (Connection conn = OracleConnection.getConnection()) {
            return exchangeRateDAO.findWeeklyRates(conn);

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 최근 한 달 환율 조회
    public List<ExchangeRate> getMonthlyExchangeRates() {
        try (Connection conn = OracleConnection.getConnection()) {
            return exchangeRateDAO.findMonthlyRates(conn);

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}