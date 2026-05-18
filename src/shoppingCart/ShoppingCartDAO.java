package shoppingCart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import category.Category;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;
import product.Product;


public class ShoppingCartDAO {

	// 회원의 장바구니에 해당 상품이 있는지
    public boolean existsCartItem(int memberId, int productId) {
    	// 존재하지 않을 경우 0, 존재할 경우 1
        String sql = "SELECT COUNT(*) FROM ShoppingCart WHERE memberId = ? AND productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                // 장바구니에 해당 상품이 존재할 경우 true, 존재하지 않을 경우 false 반환
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 회원의 장바구니에 해당 상품이 몇개 들어있는지
    public int findAmount(int memberId, int productId) {
        String sql = "SELECT amount FROM ShoppingCart WHERE memberId = ? AND productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                	// DB에서 해당 상품의 개수 반환
                    return rs.getInt("amount");
                }
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        // 조회 결과가 존재하지 않을 경우 0 반환
        return 0;
    }

    // 회원의 장바구니에 새로운 상품 추가
    public void insertCartItem(int memberId, int productId, int amount) {
        String sql = "INSERT INTO ShoppingCart(productId, memberId, amount) VALUES (?, ?, ?)";
        
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, amount);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 회원의 장바구니에 이미 담아져있는 상품 개수 업데이트
    public void updateAmount(int memberId, int productId, int amount) {
        String sql = "UPDATE ShoppingCart SET amount = ? WHERE memberId = ? AND productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, productId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 회원의 장바구니에서 특정 상품 삭제
    public void deleteCartItem(int memberId, int productId) {
        String sql = "DELETE FROM ShoppingCart WHERE memberId = ? AND productId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setInt(2, productId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 회원의 장바구니에서 전체 상품 삭제
    public void deleteAllCartItems(int memberId) {
        String sql = "DELETE FROM ShoppingCart WHERE memberId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 회원 장바구니 상품 및 수량 목록 조회
    public Map<Product, Integer> findCartProductsByMemberId(int memberId) {
        Map<Product, Integer> products = new HashMap<>();

        // 회원이 장바구니에 담아둔 상품의 id, 이름, 달러가격, 원화가격, 개수
        String sql =
            "SELECT p.productId, p.productName,  p.priceUsd, p.priceKrw, p.capacity, p.categoryId, sc.amount "
            + "FROM ShoppingCart sc "
            + "JOIN Product p ON sc.productId = p.productId "
            + "WHERE sc.memberId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
            	while (rs.next()) {

            	    Product product = new Product();

            	    product.setProductId(rs.getInt("productId"));
            	    product.setProductName(rs.getString("productName"));
            	    product.setPriceUsd(rs.getBigDecimal("priceUsd"));
            	    product.setPriceKrw(rs.getBigDecimal("priceKrw"));
            	    product.setCapacity(rs.getInt("capacity"));

            	    Category category = new Category();
            	    category.setCategoryId(rs.getInt("categoryId"));

            	    product.setCategory(category);

            	    int amount = rs.getInt("amount");

            	    products.put(product, amount);
            	}
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }

        return products;
    }
}