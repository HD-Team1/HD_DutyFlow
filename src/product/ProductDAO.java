package product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import category.Category;
import common.Currency;
import common.OracleConnection;
import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.SystemException;
import product.dto.ProductDTO;

public class ProductDAO {
  
    String baseSql =
            "SELECT p.productId, p.productName, c.categoryId, c.categoryName, c.depth, " +
            "b.brandName, " +
            "NVL(st.totalAmount, 0) AS stockAmount, " +
            "p.capacity, p.priceUsd, p.priceKrw, " +
            "NVL(e.discountRate, 0) AS discountRate, " +
            "CASE WHEN e.productId IS NULL THEN 'N' ELSE 'Y' END AS hasEvent, " +
            "p.thresholdValue, " +
            "(p.priceKrw * (1 - NVL(e.discountRate, 0) / 100)) AS finalPriceKrw, " +
            "(p.priceUsd * (1 - NVL(e.discountRate, 0) / 100)) AS finalPriceUsd " +
            "FROM product p " +
            "JOIN category c ON p.categoryId = c.categoryId " +
            "JOIN brand b ON p.brandId = b.brandId " +
            "LEFT JOIN event e ON p.productId = e.productId " +
            "LEFT JOIN ( " +
            "    SELECT productId, SUM(amount) AS totalAmount " +
            "    FROM stock " +
            "    GROUP BY productId " +
            ") st ON p.productId = st.productId ";

    /**
     * product에 데이터 넣는 기능 공통 메서드화
     */
    private Product mapProduct(ResultSet rs) throws SQLException {

        Category category = Category.builder()
                .categoryId(rs.getInt("categoryId"))
                .categoryName(rs.getString("categoryName"))
                .depth(rs.getInt("depth"))
                .build();

        return Product.builder()
        		.productId(rs.getInt("productId"))
                .category(category)
                .productName(rs.getString("productName"))
                .brandName(rs.getString("brandName"))
                .capacity(rs.getInt("capacity"))
                .priceUsd(rs.getBigDecimal("finalPriceUsd"))
                .priceKrw(rs.getBigDecimal("finalPriceKrw"))
                .discountRate(rs.getInt("discountRate"))
//                .hasEvent("Y".equalsIgnoreCase(rs.getString("hasEvent")))
                .thresholdValue(rs.getInt("thresholdValue"))
                .build();
    }

    /**
     * 모든 상품 조회
     */
    public List<Product> getAllProducts() throws SystemException {

        String sql = baseSql;
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Product> productList = new ArrayList<>();

            while (rs.next()) {
                productList.add(mapProduct(rs));
            }

            return productList;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 해당하는 카테고리의 상품 조회
     */
    public List<Product> getProductsByCategory(Category category) throws SystemException {

        String sql = baseSql + "WHERE c.categoryName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getCategoryName());

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Product> productList = new ArrayList<>();

                while (rs.next()) {
                    productList.add(mapProduct(rs));
                }

                return productList;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 특정 하나의 상품을 출력하는 메서드
     */
    public Product getProductsByProductName(String productName) throws SystemException {

        String sql = baseSql + "WHERE p.productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    /**
     * 금액 범위 지정한 상품 list 보기
     */
    public List<Product> getProductsFilterByPrice(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Currency currency
    ) throws SystemException {

        String priceExpr = currency.equals(Currency.USD)
                ? "p.priceUsd"
                : "p.priceKrw";

        String finalPriceExpr =
                "(" + priceExpr + " * (1 - NVL(e.discountRate, 0) / 100))";

        String sql = baseSql
                + "WHERE " + finalPriceExpr + " BETWEEN ? AND ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, minPrice);
            pstmt.setBigDecimal(2, maxPrice);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Product> productList = new ArrayList<>();

                while (rs.next()) {
                    productList.add(mapProduct(rs));
                }

                return productList;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }    

    // BrandID 구하기
    public int findBrandIdByBrandName(String brandName) throws SystemException {

        String sql =
                "SELECT brandId " +
                "FROM Brand " +
                "WHERE brandName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("brandId");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND);
    }
    
    // CategoryID 구하기
    public int findCategoryIdByCategoryName(String categoryName) throws SystemException {

        String sql =
                "SELECT categoryId " +
                "FROM Category " +
                "WHERE categoryName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("categoryId");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND);
    }
    
    // 상품명 중복 확인
    public boolean existsByBrandNameAndProductName(String brandName, String productName) throws SystemException {

        String sql =
                "SELECT COUNT(*) AS count " +
                "FROM Product p " +
                "JOIN Brand b ON p.brandId = b.brandId " +
                "WHERE b.brandName = ? " +
                "  AND p.productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);
            pstmt.setString(2, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return false;
    }
    
    // 상품 등록
    public int insertProduct(
            int categoryId,
            int brandId,
            String productName,
            int capacity,
            BigDecimal priceUsd,
            BigDecimal priceKrw,
            int thresholdValue
    ) throws SystemException {

        String sql =
                "INSERT INTO Product ( " +
                "    categoryId, " +
                "    brandId, " +
                "    productName, " +
                "    capacity, " +
                "    priceUsd, " +
                "    priceKrw, " +
                "    thresholdValue " +
                ") VALUES ( " +
                "    ?, ?, ?, ?, ?, ?, ? " +
                ")";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, brandId);
            pstmt.setString(3, productName);
            pstmt.setInt(4, capacity);
            pstmt.setBigDecimal(5, priceUsd);
            pstmt.setBigDecimal(6, priceKrw);
            pstmt.setInt(7, thresholdValue);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 상품 삭제
    public int deleteProductByBrandNameAndProductName(String brandName, String productName) throws SystemException {

        String sql =
                "DELETE FROM Product " +
                "WHERE productId = ( " +
                "    SELECT p.productId " +
                "    FROM Product p " +
                "    JOIN Brand b ON p.brandId = b.brandId " +
                "    WHERE b.brandName = ? " +
                "      AND p.productName = ? " +
                ")";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);
            pstmt.setString(2, productName);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 상품 원화 가격 업데이트 
    public void updateAllPriceKrw(Connection conn, BigDecimal exchangeRate) {
        String sql = "UPDATE Product SET priceKrw = ROUND(priceUsd * ?, 0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, exchangeRate);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public List<ProductDTO> getProductsByBrandName(String brandName) throws SystemException {

        String sql = baseSql + "WHERE b.brandName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<ProductDTO> productList = new ArrayList<>();

                while (rs.next()) {
                    productList.add(mapProductDto(rs));
                }

                return productList;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    public Product getProductByBrandNameAndProductName(
            String brandName,
            String productName
    ) throws SystemException {

        String sql = baseSql +
                "WHERE b.brandName = ? " +
                "  AND p.productName = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, brandName);
            pstmt.setString(2, productName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    


    public List<ProductDTO> getAllProductDtos() throws SystemException {

        String sql = baseSql + "ORDER BY p.productId";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<ProductDTO> productList = new ArrayList<>();

            while (rs.next()) {
                productList.add(mapProductDto(rs));
            }

            return productList;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public ProductDTO getProductDtoByProductId(int productId) throws SystemException {

        String sql = baseSql + "WHERE p.productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapProductDto(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public Product getProductByProductId(int productId) throws SystemException {

        String sql = baseSql + "WHERE p.productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    private ProductDTO mapProductDto(ResultSet rs) throws SQLException {

        Category category = Category.builder()
                .categoryId(rs.getInt("categoryId"))
                .categoryName(rs.getString("categoryName"))
                .depth(rs.getInt("depth"))
                .build();

        return ProductDTO.builder()
                .productId(rs.getInt("productId"))
                .category(category)
                .productName(rs.getString("productName"))
                .brandName(rs.getString("brandName"))
                .capacity(rs.getInt("capacity"))
                .priceUsd(rs.getBigDecimal("priceUsd"))
                .priceKrw(rs.getBigDecimal("priceKrw"))
                .discountRate(rs.getDouble("discountRate"))
                .hasEvent("Y".equalsIgnoreCase(rs.getString("hasEvent")))
                .thresholdValue(rs.getInt("thresholdValue"))
                .finalPriceUsd(rs.getBigDecimal("finalPriceUsd"))
                .finalPriceKrw(rs.getBigDecimal("finalPriceKrw"))
                .build();
    }
}