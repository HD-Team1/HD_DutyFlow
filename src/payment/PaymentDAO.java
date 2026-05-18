package payment;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PaymentDAO {

    // 결제 요청 생성 - PENDING 상태로 저장
    public int insertPendingPayment(PaymentDTO paymentDTO) {
        String sql = "INSERT INTO Payment "
        		+ "(orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = OracleConnection.getConnection();
        		PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"paymentId"})) {

        	pstmt.setInt(1, paymentDTO.getOrderId());
        	pstmt.setString(2, "CARD");
        	pstmt.setString(3, PaymentStatus.PENDING.name());
        	pstmt.setBigDecimal(4, paymentDTO.getAmount());
        	pstmt.setString(5, paymentDTO.getCardNumberMask()); // 마스킹 처리된 카드번호 
        	pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            throw new SystemException(ErrorCode.DB_CONNECTION);

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
	
    // 결제 성공 처리
    // Payment 상태만 SUCCESS로 변경, 주문 상태 변경은 OrderState 패턴에서 처리 예정
    public void updateStatusToSuccess(int paymentId) {

    	// 결제 상태 변경 및 결제 완료 시간 저장
        String sql = "UPDATE Payment SET paymentStatus = ?, processedAt = ? WHERE paymentId = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, PaymentStatus.SUCCESS.name());
               pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
               pstmt.setInt(3, paymentId);

               pstmt.executeUpdate();

           } catch (SQLException e) {
               throw new SystemException(ErrorCode.DB_CONNECTION, e);
           }
    }

    // 결제 실패 처리
    public void updateStatusToFailed(int paymentId, String failReason) {
    	// 결제 실패 상태와 실패 사유 저장
        String sql = "UPDATE Payment SET paymentStatus = ?, processedAt = ?, failReason = ? WHERE paymentId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, PaymentStatus.FAILED.name());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, failReason);
            pstmt.setInt(4, paymentId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // PaymentStatus 업데이트
    private void updateStatus(String sql,PaymentStatus status,int paymentId) {

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, paymentId);

            pstmt.executeUpdate();

        } catch (SQLException e) {

            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    

    // paymentId로 결제 정보 조회
    // Queue에 있던 paymentId로 실제 결제 정보를 조회해서 Payment 객체 반환
    public Payment findById(int paymentId) {
        String sql = "SELECT paymentId, orderId, paymentMethod, paymentStatus, requestedAmount, cardNumberMask, requestedAt, processedAt, failReason FROM Payment WHERE paymentId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paymentId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                	return Payment.builder()
                	        .paymentId(rs.getInt("paymentId"))
                	        .orderId(rs.getInt("orderId"))
                	        .paymentMethod(rs.getString("paymentMethod"))
                	        .paymentStatus(PaymentStatus.valueOf(rs.getString("paymentStatus")))
                	        .requestedAmount(rs.getBigDecimal("requestedAmount"))
                	        .cardNumberMask(rs.getString("cardNumberMask"))
                	        .requestedAt(rs.getTimestamp("requestedAt").toLocalDateTime())
                	        .processedAt(rs.getTimestamp("processedAt") == null
                	                ? null
                	                : rs.getTimestamp("processedAt").toLocalDateTime()) // PENDING 상태인 경우 아직 처리 전이므로 processedAt = null
                	        .failReason(rs.getString("failReason"))
                	        .build();
                }
            }

            return null;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

 // orderId로 SUCCESS 상태의 payment 조회
    public Payment findSuccessByOrderId(int orderId) {

        String sql =
            "SELECT paymentId, orderId, paymentMethod, paymentStatus, " +
            "requestedAmount, cardNumberMask, requestedAt, processedAt, failReason " +
            "FROM Payment " +
            "WHERE orderId = ? " +
            "AND paymentStatus = 'SUCCESS' " +
            "ORDER BY paymentId DESC FETCH FIRST 1 ROWS ONLY";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return Payment.builder()
                            .paymentId(rs.getInt("paymentId"))
                            .orderId(rs.getInt("orderId"))
                            .paymentMethod(rs.getString("paymentMethod"))
                            .paymentStatus(PaymentStatus.valueOf(rs.getString("paymentStatus")))
                            .requestedAmount(rs.getBigDecimal("requestedAmount"))
                            .cardNumberMask(rs.getString("cardNumberMask"))
                            .requestedAt(rs.getTimestamp("requestedAt").toLocalDateTime())
                            .processedAt(
                                    rs.getTimestamp("processedAt") == null
                                            ? null
                                            : rs.getTimestamp("processedAt").toLocalDateTime()
                            )
                            .failReason(rs.getString("failReason"))
                            .build();
                }
            }

            return null;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 결제 상태 PROCESSING으로 변경
    // 결제 대기 Queue에서 Worker가 결제 처리를 위해 꺼낼 때
    public void updateStatusToProcessing(int paymentId) {
        String sql = "UPDATE Payment SET paymentStatus = ? WHERE paymentId = ?";

        updateStatus(sql, PaymentStatus.PROCESSING, paymentId);
    }
    
    // 결제 취소 처리
    public void updateStatusToCanceled(int paymentId) {
        String sql = "UPDATE Payment "
                + "SET paymentStatus = ?, processedAt = ? "
                + "WHERE paymentId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, PaymentStatus.CANCELED.name());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(3, paymentId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}