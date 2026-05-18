package membership;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Grade;
import common.OracleConnection;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;

public class MembershipDAO {

    // 회원 등급 조회
    public Grade findGradeByMemberId(int memberId) {

        String sql = "SELECT grade FROM Member WHERE memberId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                	// enum으로 변환해서 리턴
                    return Grade.valueOf(rs.getString("grade"));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
    }
    
    // 등급 선정일 조회
    // 로그인 시 등급 기간이 만료되었는지 확인하기 위함
    public LocalDate findGradeSelectionDate(int memberId) {

        String sql = "SELECT gradeSelectionDate FROM Member WHERE memberId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date date = rs.getDate("gradeSelectionDate");
                    return date.toLocalDate();
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
    }
    
    // 회원 등급 갱신
    public void updateGrade(int memberId, Grade grade) {

        String sql =
            "UPDATE Member SET grade = ?, gradeSelectionDate = ? WHERE memberId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, grade.name());
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            pstmt.setInt(3, memberId);

            int result = pstmt.executeUpdate();

            // 업데이트된 행이 없을 경우 대상 회원이 존재하지 않는 것으로 간주
            if (result == 0) { 
                throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 최근 1년 구매 금액 조회
    public BigDecimal sumPurchaseAmountLastOneYear(int memberId) {

    	String sql = "SELECT NVL(SUM(totalAmount), 0) "
    					+ "FROM Orders "
    					+ "WHERE memberId = ? "
    					+ "AND orderState IN ('ORDERED', 'PAID', 'PICKUP_RESERVED', 'PICKED_UP') "
    					+ "AND orderedAt >= ADD_MONTHS(SYSDATE, -12)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        

        return BigDecimal.ZERO;
    }
    
}
