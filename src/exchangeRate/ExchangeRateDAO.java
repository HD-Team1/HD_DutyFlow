package exchangeRate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import exception.ErrorCode;
import exception.SystemException;

public class ExchangeRateDAO {
	// 최신 환율 조회
    public BigDecimal findLatestRate(Connection conn) {
        String sql = "SELECT exchangeRate FROM ExchangeRate WHERE isLatest = 'Y'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("exchangeRate");
            }

            return null;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 기존 최신 환율의 flag값을 N으로 변경
    public void updateLatestToN(Connection conn) {
        String sql = "UPDATE ExchangeRate SET isLatest = 'N' WHERE isLatest = 'Y'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 오늘 환율 저장
    public void insertTodayRate(Connection conn, BigDecimal exchangeRate) {
        String sql =
            "INSERT INTO ExchangeRate(exchangeDate, exchangeRate, isLatest) "
        	+ "VALUES (TRUNC(SYSDATE), ?, 'Y')"; // 가장 최신 환율이므로 isLastest = Y로 설정

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, exchangeRate);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 오늘 환율 조회
    public BigDecimal findTodayRate(Connection conn) {
        String sql =
                "SELECT exchangeRate " 
              + "FROM ExchangeRate "
              + "WHERE exchangeDate = TRUNC(SYSDATE)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("exchangeRate");
            }

            // 조회 결과 없으면 null 반환
            return null;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
	
	 // 최근 일주일 환율 조회
	 public List<ExchangeRate> findWeeklyRates(Connection conn) {
	     String sql =
	             "SELECT exchangeDate, exchangeRate, isLatest " +
	             "FROM ExchangeRate " +
	             "WHERE exchangeDate >= TRUNC(SYSDATE) - 6 " +
	             "ORDER BY exchangeDate DESC";
	
	     return findRates(conn, sql);
	 }
	
	 // 최근 한 달 환율 조회
	 public List<ExchangeRate> findMonthlyRates(Connection conn) {
	     String sql =
	             "SELECT exchangeDate, exchangeRate, isLatest " +
	             "FROM ExchangeRate " +
	             "WHERE exchangeDate >= ADD_MONTHS(TRUNC(SYSDATE), -1) " +
	             "ORDER BY exchangeDate DESC";
	
	     return findRates(conn, sql);
	 }
	
	 // 환율 목록 매핑
	 private List<ExchangeRate> findRates(Connection conn, String sql) {
	     List<ExchangeRate> exchangeRates = new ArrayList<>();
	
	     try (PreparedStatement pstmt = conn.prepareStatement(sql);
	          ResultSet rs = pstmt.executeQuery()) {
	
	         while (rs.next()) {
	             ExchangeRate exchangeRate = new ExchangeRate();
	
	             exchangeRate.setExchangeDate(
	                     rs.getDate("exchangeDate").toLocalDate()
	             );
	
	             exchangeRate.setExchangeRate(
	                     rs.getBigDecimal("exchangeRate")
	             );
	
	             exchangeRate.setIsLatest(
	                     rs.getString("isLatest").charAt(0)
	             );
	
	             exchangeRates.add(exchangeRate);
	         }
	
	         return exchangeRates;
	
	     } catch (SQLException e) {
	         throw new SystemException(ErrorCode.DB_CONNECTION, e);
	     }
	 }
}