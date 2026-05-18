package admin.brandmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import admin.brandmanager.dto.BrandManager;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class BrandManagerDao {

    public BrandManager findByManagerId(int managerId) {

        String sql =
                "SELECT m.managerId, " +
                "       m.managerName, " +
                "       m.managerType, " +
                "       m.password, " +
                "       sm.annualLeaveCount, " +
                "       b.brandName " +
                "FROM Manager m " +
                "JOIN ShopManager sm ON m.managerId = sm.managerId " +
                "JOIN Brand b ON m.managerId = b.managerId " +
                "WHERE m.managerId = ? " +
                "  AND m.managerType = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, managerId);
            pstmt.setString(2, "SHOP");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToBrandManager(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    private BrandManager mapToBrandManager(ResultSet rs) throws SQLException {
        return new BrandManager(
                rs.getInt("managerId"),
                rs.getString("managerName"),
                rs.getString("managerType"),
                rs.getString("password"),
                rs.getInt("annualLeaveCount"),
                rs.getString("brandName")
        );
    }
}