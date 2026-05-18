package admin.airportmanager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import admin.airportmanager.dto.AirportManagerLoginDto;
import admin.airportmanager.dto.PickUpListDTO;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import member.Member;

public class AirportManagerDao {
	
	public AirportManager findById(int managerId) {
        String sql =
            " SELECT mg.managerId, mg.managerName, mg.managerType, mg.password, am.shiftTime " 
            + " FROM   Manager mg " 
            + " JOIN   AirportManager am ON mg.managerId = am.managerId " 
            + " WHERE  mg.managerId  = ?  " 
            + " AND    mg.managerType = 'AIRPORT' ";
 
        try (Connection conn = OracleConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, managerId);
 
            try (ResultSet rs = pstmt.executeQuery()) {
            	
                if (rs.next()) {
                	
                    return new AirportManager(
                        rs.getInt("managerId"),
                        rs.getString("managerName"),
                        rs.getString("managerType"),
                        rs.getString("password"),
                        rs.getTimestamp("shiftTime") != null ? rs.getTimestamp("shiftTime").toLocalDateTime() : null
                    );
                }
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        return null; 
    }
	
	private static final String BASE_SQL =
	        " SELECT p.pickupId, "
	        + "       p.pickupAvailableAt, "
	        + "       p.pickedUpAt, "
	        + "       o.orderId, "
	        + "       o.orderState, "
	        + "       o.orderedAt, "
	        + "       m.memberId, "
	        + "       m.name       AS memberName, "
	        + "       m.passportNumber, "
	        + "       m.grade, "
	        + "       f.flightCode, "
	        + "       f.departureAt "
	        + " FROM  Pickup    p "
	        + " JOIN  Orders    o  ON p.orderId       = o.orderId "
	        + " JOIN  Member    m  ON o.memberId       = m.memberId "
	        + " JOIN  FlightBook fb ON o.reservationId = fb.reservationId "
	        + " JOIN  Flight    f  ON fb.flightId      = f.flightId ";
	
	public List<PickUpListDTO> getAllPickUpList() {
        String sql = BASE_SQL
            + " ORDER BY p.pickupAvailableAt ASC, p.pickupId ASC ";

        List<PickUpListDTO> list = new ArrayList<>();

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        return list;
    }
	
	public List<PickUpListDTO> getAllPickUpListByMember(Member member) {
        String sql = BASE_SQL
            + " WHERE  m.memberId = ? "
            + " ORDER BY p.pickupAvailableAt ASC, p.pickupId ASC ";

        List<PickUpListDTO> list = new ArrayList<>();

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, member.getMemberId()); 

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        return list;
    }
	
	public List<PickUpListDTO> getAllPickUpListByDateRange(LocalDate start, LocalDate end) {
        String sql = BASE_SQL
            + " WHERE  TRUNC(p.pickupAvailableAt) >= ? "
            + " AND    TRUNC(p.pickupAvailableAt) <= ? "
            + " ORDER BY p.pickupAvailableAt ASC, p.pickupId ASC ";

        List<PickUpListDTO> list = new ArrayList<>();

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // LocalDate → java.sql.Date 변환 (Oracle DATE 바인딩)
            pstmt.setDate(1, java.sql.Date.valueOf(start));
            pstmt.setDate(2, java.sql.Date.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
        return list;
    }
	
	private PickUpListDTO mapRow(ResultSet rs) throws SQLException {
        java.sql.Timestamp pickedUpAtTs = rs.getTimestamp("pickedUpAt");

        return PickUpListDTO.builder()
            .pickupId(rs.getInt("pickupId"))
            .pickupAvailableAt(rs.getTimestamp("pickupAvailableAt").toLocalDateTime())
            .pickedUpAt(pickedUpAtTs != null ? pickedUpAtTs.toLocalDateTime() : null)
            .orderId(rs.getInt("orderId"))
            .orderState(rs.getString("orderState"))
            .orderedAt(rs.getTimestamp("orderedAt").toLocalDateTime())
            .memberId(rs.getInt("memberId"))
            .memberName(rs.getString("memberName"))
            .passportNumber(rs.getString("passportNumber"))
            .grade(rs.getString("grade"))
            .flightCode(rs.getString("flightCode"))
            .departureAt(rs.getTimestamp("departureAt").toLocalDateTime())
            .build();
    }

}
