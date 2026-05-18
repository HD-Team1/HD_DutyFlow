package stock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import common.OracleConnection;

import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;

import stock.domain.Stock;
import stock.dto.StockProductDto;

public class StockDao {

    public List<StockProductDto> getAllStockByBrandName(String brandName) throws SystemException {
        List<StockProductDto> stockProductList = new ArrayList<>();

        String sql =
                "SELECT " +
                "    p.productId, p.productName, p.capacity, p.priceUsd, p.priceKrw, p.thresholdValue, " +
                "    c.categoryId, c.categoryName, c.depth, " +
                "    s.stockId, s.amount, s.manufacturedDate " +
                "FROM product p " +
                "JOIN stock s ON p.productId = s.productId " +
                "JOIN brand b ON p.brandId = b.brandId " +
                "JOIN category c ON p.categoryId = c.categoryId " +
                "WHERE b.brandName = ? " +
                "ORDER BY p.productName, s.manufacturedDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = Category.builder()
                            .categoryId(rs.getInt("categoryId"))
                            .categoryName(rs.getString("categoryName"))
                            .depth(rs.getInt("depth"))
                            .build();

                    StockProductDto dto = StockProductDto.builder()
                            .category(category)
                            .productName(rs.getString("productName"))
                            .capacity(rs.getInt("capacity"))
                            .priceUsd(rs.getBigDecimal("priceUsd"))
                            .priceKrw(rs.getBigDecimal("priceKrw"))
                            .thresholdValue(rs.getInt("thresholdValue"))
                            .amount(rs.getInt("amount"))
                            .manufacturedDate(rs.getDate("manufacturedDate") != null
                                    ? rs.getDate("manufacturedDate").toLocalDate()
                                    : null)
                            .build();

                    stockProductList.add(dto);
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return stockProductList;
    }

    public List<Stock> findByProductIdOrderByManufacturedDate(int productId) throws SystemException {
        List<Stock> stockList = new ArrayList<>();

        String sql =
                "SELECT stockId, productId, manufacturedDate, amount " +
                "FROM stock " +
                "WHERE productId = ? " +
                "  AND amount > 0 " +
                "ORDER BY manufacturedDate ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Stock stock = Stock.builder()
                            .stockId(rs.getInt("stockId"))
                            .productId(rs.getInt("productId"))
                            .manufacturedDate(rs.getDate("manufacturedDate") != null
                                    ? rs.getDate("manufacturedDate").toLocalDate()
                                    : null)
                            .amount(rs.getInt("amount"))
                            .build();

                    stockList.add(stock);
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return stockList;
    }

    public List<Stock> findByProductNameOrderByManufacturedDate(String productName) throws SystemException {
        int productId = findProductIdByProductName(productName);
        return findByProductIdOrderByManufacturedDate(productId);
    }

    public int getTotalAmountByProductId(int productId) throws SystemException {
        String sql =
                "SELECT NVL(SUM(amount), 0) AS totalAmount " +
                "FROM stock " +
                "WHERE productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalAmount");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return 0;
    }

    public int getTotalAmountByProductName(String productName) throws SystemException {
        int productId = findProductIdByProductName(productName);
        return getTotalAmountByProductId(productId);
    }

    public int updateAmount(int stockId, int amount) throws SystemException {
        String sql =
                "UPDATE stock " +
                "SET amount = ? " +
                "WHERE stockId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setInt(2, stockId);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int insertStock(Stock stock) throws SystemException {
        String sql =
                "INSERT INTO stock (productId, manufacturedDate, amount) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stock.getProductId());
            pstmt.setDate(2, java.sql.Date.valueOf(stock.getManufacturedDate()));
            pstmt.setInt(3, stock.getAmount());

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int findProductIdByProductName(String productName) throws SystemException {
        String sql =
                "SELECT productId " +
                "FROM product " +
                "WHERE productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("productId");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(
                ErrorCode.PRODUCT_NOT_FOUND,
                new Exception("상품을 찾을 수 없습니다. 상품명: " + productName)
        );
    }

    public int deleteZeroAmountStocks() throws SystemException {
        String sql =
                "DELETE FROM stock " +
                "WHERE amount = 0";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public int getThresholdValueByProductId(int productId) throws SystemException {
        String sql =
                "SELECT thresholdValue " +
                "FROM product " +
                "WHERE productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("thresholdValue");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(
                ErrorCode.PRODUCT_NOT_FOUND,
                new Exception("상품 임계값 정보를 찾을 수 없습니다. productId=" + productId)
        );
    }

    public String getBrandNameByProductId(int productId) throws SystemException {
        String sql =
                "SELECT b.brandName " +
                "FROM product p " +
                "JOIN brand b ON p.brandId = b.brandId " +
                "WHERE p.productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("brandName");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(
                ErrorCode.PRODUCT_NOT_FOUND,
                new Exception("상품의 브랜드 정보를 찾을 수 없습니다. productId=" + productId)
        );
    }

    public String getBrandNameByProductName(String productName) throws SystemException {
        String sql =
                "SELECT b.brandName " +
                "FROM product p " +
                "JOIN brand b ON p.brandId = b.brandId " +
                "WHERE p.productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("brandName");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(
                ErrorCode.PRODUCT_NOT_FOUND,
                new Exception("상품의 브랜드 정보를 찾을 수 없습니다. 상품명: " + productName)
        );
    }
}