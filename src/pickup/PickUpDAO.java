package pickup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import common.Grade;
import pickup.dto.AppendQueueDTO;
import pickup.dto.PickUpDTO;
// DBUtil 등 필요한 임포트 유지

public class PickUpDAO {

	public List<PickUpDTO> getAllPickUp(String passportNum, int flightResNum) {
		List<PickUpDTO> pickUpList = new ArrayList<>();

		String sql = "" + "SELECT P.pickUpAvailableAt, M.passportNumber, B.reservationCode, F.departureAt\n"
				+ "FROM PickUp P \n" + "JOIN Orders O ON P.orderId = O.orderId\n"
				+ "JOIN Member M ON O.memberId = M.memberId\n"
				+ "JOIN FlightBook B ON O.reservationId = B.reservationId AND M.memberId = B.memberId\n"
				+ "JOIN Flight F ON B.flightId = F.flightId\n" + "WHERE M.passportNumber = ? AND B.reservationId = ?";

		try (Connection conn = OracleConnection.getConnection(); // 팀의 DB 연결 클래스명에 맞게 수정
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

			try (ResultSet rs = pstmt.executeQuery()) {
				boolean hasData = false;
				while (rs.next()) {
					hasData = true;
					PickUpDTO dto = PickUpDTO.builder()
							.pickupAvailableAt(rs.getObject("pickupAvailableAt", LocalDateTime.class))
							.passportNumber(rs.getString("passportNumber"))
							.reservationCode(rs.getString("reservationCode"))
							.departureAt(rs.getObject("departureAt", LocalDateTime.class)).build();
					pickUpList.add(dto);
				}
				if (!hasData) {
					// 데이터가 없으면 DataNotFoundException 던짐
					throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, new Exception("일치하는 예약/여권 정보가 없습니다."));
				}
			}
		} catch (SQLException e) {
			// SQL 예외는 SystemException으로 래핑하여 던짐
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return pickUpList;
	}

	public AppendQueueDTO getAppendingInfo(String passportNum, int flightResNum) {
		AppendQueueDTO aqdto = null;

		String sql = "" + "SELECT F.flightCode, F.departureAt, F.isDelayed, B.reservationCode, M.memberId, M.grade, M.name \n"
				+ "FROM Flight F \n" + "JOIN FlightBook B ON F.flightId = B.flightId \n"
				+ "JOIN Member M ON B.memberId = M.memberId \n" + "WHERE M.passportNumber = ? AND B.reservationId = ?";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					aqdto = AppendQueueDTO.builder().flightCode(rs.getString("flightCode"))
							.departureAt(rs.getObject("departureAt", LocalDateTime.class))
							.isDelayed(rs.getInt("isDelayed")).reservationCode(rs.getString("reservationCode"))
							.memberId(rs.getInt("memberId"))
							.grade(Grade.valueOf(rs.getString("grade").toUpperCase())).name(rs.getString("name"))
							.build();
				} else {
					throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND, new Exception("큐 삽입용 회원 정보를 찾을 수 없습니다."));
				}
			}
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return aqdto;
	}

	// [PickUpDAO.java 에 추가할 메서드 1]
	// 여권번호와 예약번호를 통해 대상 주문의 orderId를 가져옵니다.
	public int getOrderIdForPickup(String passportNum, int flightResNum) throws SystemException {
		String sql = "SELECT O.orderId " + "FROM Orders O " + "JOIN Member M ON O.memberId = M.memberId "
				+ "JOIN FlightBook B ON O.reservationId = B.reservationId "
				+ "WHERE M.passportNumber = ? AND B.reservationId = ?";

		try (Connection conn = OracleConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, passportNum);
			pstmt.setInt(2, flightResNum);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("orderId");
				} else {
					throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND,
							new Exception("일치하는 주문(OrderId)을 찾을 수 없습니다."));
				}
			}
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
	}

	/**
	 * Orders 테이블의 상태를 업데이트하고, pickedUpAt이 전달되면 Pickup 테이블도 함께 갱신합니다.
	 * - PICKED_UP: updateOrderAndPickupState(orderId, "PICKED_UP", CurrentTime.curTime)
	 * - NO_SHOW:   updateOrderAndPickupState(orderId, "NO_SHOW", null)
	 */
	public void updateOrderAndPickupState(int orderId, String newState, LocalDateTime pickedUpAt)
			throws SystemException {
		String updateOrderSql = "UPDATE Orders SET orderState = ? WHERE orderId = ?";
		String updatePickupSql = "UPDATE Pickup SET pickedUpAt = ? WHERE orderId = ?";

		Connection conn = null;
		try {
			conn = OracleConnection.getConnection();
			conn.setAutoCommit(false);

			// 1. Orders 테이블 상태 업데이트
			try (PreparedStatement pstmt1 = conn.prepareStatement(updateOrderSql)) {
				pstmt1.setString(1, newState);
				pstmt1.setInt(2, orderId);

				int affected = pstmt1.executeUpdate();
				if (affected == 0) {
					throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND,
							new Exception("업데이트 대상 주문이 없습니다. orderId=" + orderId));
				}
			}

			// 2. pickedUpAt이 있을 때만 Pickup 테이블 수령시간 업데이트
			if (pickedUpAt != null) {
				try (PreparedStatement pstmt2 = conn.prepareStatement(updatePickupSql)) {
					pstmt2.setTimestamp(1, java.sql.Timestamp.valueOf(pickedUpAt));
					pstmt2.setInt(2, orderId);
					pstmt2.executeUpdate();
				}
			}

			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try { conn.rollback(); } catch (SQLException ex) { /* ignored */ }
			}
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { /* ignored */ }
			}
		}
	}

}