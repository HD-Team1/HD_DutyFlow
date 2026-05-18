package flight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class FlightDAO {

	private FlightDTO mapRowToFlightDTO(ResultSet rs) throws SQLException {
		return new FlightDTO(
			rs.getInt("flightId"),
			rs.getString("flightCode"),
			rs.getTimestamp("departureAt").toLocalDateTime(),
			rs.getInt("isDelayed")
		);
	}

	// 항공편 정보 조회 (항공편 코드)
	public FlightDTO getFlightByCode(String flightCode) {

		String sql = "SELECT flightId, flightCode, departureAt, isDelayed "
				+ "FROM Flight "
				+ "WHERE flightCode = ?";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, flightCode);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					return mapRowToFlightDTO(rs);
				}
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return null;
	}

	// 탑승 예약 정보 조회 (회원ID + 항공편코드)
	public FlightBookDTO getBookByMemberAndFlight(int memberId, String flightCode) {

		String sql = "SELECT b.reservationId, b.memberId, b.flightId, b.reservationCode "
				+ "FROM FlightBook b "
				+ "JOIN Flight f ON b.flightId = f.flightId "
				+ "WHERE b.memberId = ? AND f.flightCode = ?";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, memberId);
			pstmt.setString(2, flightCode);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {

					return new FlightBookDTO(
						rs.getInt("reservationId"),
						rs.getInt("memberId"),
						rs.getInt("flightId"),
						rs.getString("reservationCode")
					);
				}
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return null;
	}

	// 항공 정보 조회 (비행기 예약 코드)
	public FlightDTO getFlightByReservationCode(String reservationCode) {

		String sql = "SELECT f.flightId, f.flightCode, f.departureAt, f.isDelayed "
				+ "FROM Flight f "
				+ "JOIN FlightBook b ON f.flightId = b.flightId "
				+ "WHERE b.reservationCode = ?";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, reservationCode);

			try (ResultSet rs = pstmt.executeQuery()) {

				if (rs.next()) {
					return mapRowToFlightDTO(rs);
				}
			}

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}

		return null;
	}
	
	// 회원 ID로 항공 정보 조회
	// 회원 ID로 FlightBook 정보 조회
	public List<FlightBookDTO> getFlightBookByMemberId(int memberId) {

	    String sql =
	            "SELECT b.reservationId, b.memberId, b.flightId, b.reservationCode, " +
	            "       f.flightCode, f.departureAt, f.isDelayed " +
	            "FROM FlightBook b " +
	            "JOIN Flight f ON b.flightId = f.flightId " +
	            "WHERE b.memberId = ? " +
	            "ORDER BY f.departureAt DESC";

	    List<FlightBookDTO> flightList = new ArrayList<>();

	    try (
	            Connection conn = OracleConnection.getConnection();
	            PreparedStatement pstmt = conn.prepareStatement(sql)
	    ) {

	        pstmt.setInt(1, memberId);

	        try (ResultSet rs = pstmt.executeQuery()) {

	            while (rs.next()) {

	                FlightBookDTO dto = FlightBookDTO.builder()
	                        .reservationId(rs.getInt("reservationId"))
	                        .memberId(rs.getInt("memberId"))
	                        .flightId(rs.getInt("flightId"))
	                        .reservationCode(rs.getString("reservationCode"))
	                        .build();

	                flightList.add(dto);
	            }
	        }

	        return flightList;

	    } catch (SQLException e) {

	        throw new SystemException(ErrorCode.DB_CONNECTION, e);
	    }
	}
	
	// 지연 시각 업데이트 (항공편코드 + 지연 시)
	public void updateDelayedFlight(String flightCode, LocalDateTime newDepartureAt) {

		String sql = "UPDATE Flight "
				+ "SET departureAt = ?, isDelayed = 1 "
				+ "WHERE flightCode = ?";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setTimestamp(1, Timestamp.valueOf(newDepartureAt));
			pstmt.setString(2, flightCode);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
	}
}