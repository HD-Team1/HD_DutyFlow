package regulation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class RegulationDAO {

	// categoryId로 규정 조회 
    public RegulationDTO getRegulationByCategoryId(int categoryId) {

        String sql =
                "SELECT regulationId, categoryId, limitCapacity, establishedDate, overageRate " +
                "FROM Regulation " +
                "WHERE categoryId = ?";

        try (
                Connection conn = OracleConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    return RegulationDTO.builder()
                            .regulationId(rs.getInt("regulationId"))
                            .categoryId(rs.getInt("categoryId"))
                            .limitCapacity(rs.getInt("limitCapacity"))
                            .establishedDate(
                                    rs.getDate("establishedDate") != null
                                            ? rs.getDate("establishedDate").toLocalDate()
                                            : null
                            )
                            .overageRate(rs.getInt("overageRate"))
                            .build();
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return null;
    }

    // categoryName으로 규정 조회
    public RegulationDTO getByCategoryName(String categoryName) {

        String sql =
                "SELECT r.regulationId, r.categoryId, r.limitCapacity, " +
                "r.establishedDate, r.overageRate " +
                "FROM Regulation r " +
                "JOIN Category c ON r.categoryId = c.categoryId " +
                "WHERE c.categoryName = ?";

        try (
                Connection conn = OracleConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, categoryName);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    return RegulationDTO.builder()
                            .regulationId(rs.getInt("regulationId"))
                            .categoryId(rs.getInt("categoryId"))
                            .limitCapacity(rs.getInt("limitCapacity"))
                            .establishedDate(
                                    rs.getDate("establishedDate") != null
                                            ? rs.getDate("establishedDate").toLocalDate()
                                            : null
                            )
                            .overageRate(rs.getInt("overageRate"))
                            .build();
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return null;
    }
}