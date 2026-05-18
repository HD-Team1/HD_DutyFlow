package stock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.OracleConnection;

import exception.ErrorCode;
import exception.SystemException;

import stock.domain.StockPurchase;
import stock.domain.StockPurchaseStatus;
import stock.dto.StockPurchaseHistoryDto;

public class StockPurchaseDao {

    public int insertPurchaseByProductName(String productName, int amount) throws SystemException {

        String sql =
                "INSERT INTO StockPurchase (productId, purchaseDate, amount, status) " +
                "SELECT productId, SYSDATE, ?, ? " +
                "FROM Product " +
                "WHERE productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setString(2, StockPurchaseStatus.REQUESTED.name());
            pstmt.setString(3, productName);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public StockPurchase findById(int purchaseId) throws SystemException {

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "WHERE purchaseId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, purchaseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToStockPurchase(rs);
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return null;
    }

    public List<StockPurchase> findAll() throws SystemException {

        List<StockPurchase> purchaseList = new ArrayList<>();

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "ORDER BY purchaseId DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                purchaseList.add(mapToStockPurchase(rs));
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public List<StockPurchaseHistoryDto> findByProductName(String productName) throws SystemException {

        List<StockPurchaseHistoryDto> purchaseList = new ArrayList<>();

        String sql =
                "SELECT sp.purchaseId, " +
                "       sp.productId, " +
                "       p.productName, " +
                "       b.brandName, " +
                "       c.categoryName, " +
                "       p.priceUsd, " +
                "       p.priceKrw, " +
                "       p.thresholdValue, " +
                "       sp.purchaseDate, " +
                "       sp.amount, " +
                "       sp.status " +
                "FROM StockPurchase sp " +
                "JOIN Product p ON sp.productId = p.productId " +
                "JOIN Brand b ON p.brandId = b.brandId " +
                "JOIN Category c ON p.categoryId = c.categoryId " +
                "WHERE p.productName = ? " +
                "ORDER BY sp.purchaseId DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseList.add(mapToStockPurchaseHistoryDto(rs));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public List<StockPurchaseHistoryDto> findByStatus(StockPurchaseStatus status) throws SystemException {

        List<StockPurchaseHistoryDto> purchaseList = new ArrayList<>();

        String sql =
                "SELECT sp.purchaseId, " +
                "       sp.productId, " +
                "       p.productName, " +
                "       b.brandName, " +
                "       c.categoryName, " +
                "       p.priceUsd, " +
                "       p.priceKrw, " +
                "       p.thresholdValue, " +
                "       sp.purchaseDate, " +
                "       sp.amount, " +
                "       sp.status " +
                "FROM StockPurchase sp " +
                "JOIN Product p ON sp.productId = p.productId " +
                "JOIN Brand b ON p.brandId = b.brandId " +
                "JOIN Category c ON p.categoryId = c.categoryId " +
                "WHERE sp.status = ? " +
                "ORDER BY sp.purchaseDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseList.add(mapToStockPurchaseHistoryDto(rs));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public List<StockPurchaseHistoryDto> findRequestedPurchases() throws SystemException {
        return findByStatus(StockPurchaseStatus.REQUESTED);
    }

    public List<StockPurchaseHistoryDto> findReceivedPurchases() throws SystemException {
        return findByStatus(StockPurchaseStatus.RECEIVED);
    }

    public int updateStatus(int purchaseId, StockPurchaseStatus status) throws SystemException {

        String sql =
                "UPDATE StockPurchase " +
                "SET status = ? " +
                "WHERE purchaseId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setInt(2, purchaseId);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public List<StockPurchase> findReceivablePurchases() throws SystemException {

        List<StockPurchase> purchaseList = new ArrayList<>();

        String sql =
                "SELECT purchaseId, productId, purchaseDate, amount, status " +
                "FROM StockPurchase " +
                "WHERE status = ? " +
                "  AND purchaseDate <= SYSDATE - (3 / 1440) " +
                "ORDER BY purchaseDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, StockPurchaseStatus.REQUESTED.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseList.add(mapToStockPurchase(rs));
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseList;
    }

    public int updateStatusIfCurrentStatus(
            int purchaseId,
            StockPurchaseStatus currentStatus,
            StockPurchaseStatus nextStatus
    ) throws SystemException {

        String sql =
                "UPDATE StockPurchase " +
                "SET status = ? " +
                "WHERE purchaseId = ? " +
                "  AND status = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nextStatus.name());
            pstmt.setInt(2, purchaseId);
            pstmt.setString(3, currentStatus.name());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int cancelPurchase(int purchaseId) throws SystemException {
        return updateStatusIfCurrentStatus(
                purchaseId,
                StockPurchaseStatus.REQUESTED,
                StockPurchaseStatus.CANCELED
        );
    }

    public int markAsReceived(int purchaseId) throws SystemException {
        return updateStatusIfCurrentStatus(
                purchaseId,
                StockPurchaseStatus.REQUESTED,
                StockPurchaseStatus.RECEIVED
        );
    }

    public List<StockPurchaseHistoryDto> findStockPurchasesHistoryByBrandName(String brandName) throws SystemException {

        List<StockPurchaseHistoryDto> purchaseHistoryList = new ArrayList<>();

        String sql =
                "SELECT sp.purchaseId, " +
                "       sp.productId, " +
                "       p.productName, " +
                "       b.brandName, " +
                "       c.categoryName, " +
                "       p.priceUsd, " +
                "       p.priceKrw, " +
                "       p.thresholdValue, " +
                "       sp.purchaseDate, " +
                "       sp.amount, " +
                "       sp.status " +
                "FROM StockPurchase sp " +
                "JOIN Product p ON sp.productId = p.productId " +
                "JOIN Brand b ON p.brandId = b.brandId " +
                "JOIN Category c ON p.categoryId = c.categoryId " +
                "WHERE b.brandName = ? " +
                "ORDER BY sp.purchaseId DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchaseHistoryList.add(mapToStockPurchaseHistoryDto(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[StockPurchaseDao] SQL 실행 실패");
            System.err.println("원인: " + e.getMessage());
            e.printStackTrace();
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return purchaseHistoryList;
    }

    private StockPurchase mapToStockPurchase(ResultSet rs) throws SQLException {
        return StockPurchase.builder()
                .purchaseId(rs.getInt("purchaseId"))
                .productId(rs.getInt("productId"))
                .purchaseDate(rs.getTimestamp("purchaseDate") != null
                        ? rs.getTimestamp("purchaseDate").toLocalDateTime()
                        : null)
                .amount(rs.getInt("amount"))
                .status(StockPurchaseStatus.valueOf(rs.getString("status")))
                .build();
    }

    private StockPurchaseHistoryDto mapToStockPurchaseHistoryDto(ResultSet rs) throws SQLException {
        return StockPurchaseHistoryDto.builder()
                .purchaseId(rs.getInt("purchaseId"))
                .productId(rs.getInt("productId"))
                .productName(rs.getString("productName"))
                .brandName(rs.getString("brandName"))
                .categoryName(rs.getString("categoryName"))
                .priceUsd(rs.getBigDecimal("priceUsd"))
                .priceKrw(rs.getBigDecimal("priceKrw"))
                .thresholdValue(rs.getInt("thresholdValue"))
                .purchaseDate(rs.getTimestamp("purchaseDate") != null
                        ? rs.getTimestamp("purchaseDate").toLocalDateTime()
                        : null)
                .amount(rs.getInt("amount"))
                .status(StockPurchaseStatus.valueOf(rs.getString("status")))
                .build();
    }
}