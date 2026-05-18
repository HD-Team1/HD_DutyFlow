package shoppingCart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.ValidationException;
import product.Product;
import shoppingCart.dto.CartItemDTO;
import shoppingCart.dto.TotalCartDTO;

public class ShoppingCartService {

	private final ShoppingCartDAO shoppingCartDAO = new ShoppingCartDAO();

	// 장바구니에 상품 추가
	public void addToCart(int memberId, Product p, int wishAmount) {

		// 상품 정보가 없는 경우
		if (p == null) {
			throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
		}

		// 추가하려는 수량이 0 이하인 경우
		if (wishAmount <= 0) {
			throw new ValidationException(ErrorCode.INVALID_QUANTITY);
		}

		int productId = p.getProductId();

		// 이미 장바구니에 존재하는 상품이면 기존 수량에 추가
		// 존재하지 않으면 새로 추가
		if (shoppingCartDAO.existsCartItem(memberId, productId)) {
			int currentAmount = shoppingCartDAO.findAmount(memberId, productId);
			shoppingCartDAO.updateAmount(memberId, productId, currentAmount + wishAmount);
		} else {
			shoppingCartDAO.insertCartItem(memberId, productId, wishAmount);
		}
	}

	// 장바구니 내 특정 상품 수량 변경
	public void updateQuantity(int memberId, Product p, int newAmount) {

		// 상품 정보가 없는 경우
		if (p == null) {
			throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
		}

		// 수량이 0 이하인 경우
		if (newAmount <= 0) {
			throw new ValidationException(ErrorCode.INVALID_QUANTITY);
		}

		int productId = p.getProductId();

		// 장바구니에 존재하지 않는 상품인 경우
		if (!shoppingCartDAO.existsCartItem(memberId, productId)) {
			throw new DataNotFoundException(ErrorCode.CART_PRODUCT_NOT_FOUND);
		}

		// 상품 수량 변경
		shoppingCartDAO.updateAmount(memberId, productId, newAmount);
	}

	// 장바구니 조회
	public TotalCartDTO getCart(int memberId) {

		Map<Product, Integer> products = shoppingCartDAO.findCartProductsByMemberId(memberId);

		if (products.isEmpty()) {
			return new TotalCartDTO(List.of(), 0, BigDecimal.ZERO, BigDecimal.ZERO); // 비어있는 장바구니 DTO 객체를 생성해서 반환함
		}

		// DTO 생성 중 일부만 처리되고 예외가 발생하는 상황을 방지하기 위해
		// 반환 전에 상품 정보 및 수량 데이터를 먼저 검증
		for (Map.Entry<Product, Integer> entry : products.entrySet()) {
			Product product = entry.getKey();
			int quantity = entry.getValue();

			// 상품 정보가 없는 경우
			if (product == null) {
				throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
			}

			// 수량이 0 이하인 경우
			if (quantity <= 0) {
				throw new ValidationException(ErrorCode.INVALID_QUANTITY);
			}

			// 상품 가격 정보가 없는 경우
			if (product.getPriceUsd() == null || product.getPriceKrw() == null) {
				throw new ValidationException(ErrorCode.INVALID_PRODUCT_PRICE);
			}
		}

		List<CartItemDTO> items = new ArrayList<>();

		int totalQuantity = 0;

		BigDecimal totalDollarPrice = BigDecimal.ZERO;
		BigDecimal totalWonPrice = BigDecimal.ZERO;

		for (Map.Entry<Product, Integer> entry : products.entrySet()) {
			Product product = entry.getKey();
			int quantity = entry.getValue();

			BigDecimal dollarPrice = product.getPriceUsd().multiply(BigDecimal.valueOf(quantity));
			BigDecimal wonPrice = product.getPriceKrw().multiply(BigDecimal.valueOf(quantity));

			// 상품별 DTO 생성
			items.add(new CartItemDTO(product.getProductId(), product.getProductName(), quantity, dollarPrice, wonPrice,
					product.getCategory().getCategoryId(), product.getCapacity()));

			// 총합 계산
			totalQuantity += quantity;
			totalDollarPrice = totalDollarPrice.add(dollarPrice);
			totalWonPrice = totalWonPrice.add(wonPrice);
		}

		return new TotalCartDTO(items, totalQuantity, totalDollarPrice, totalWonPrice);
	}

	// 장바구니 전체 비우기
	public void flush(int memberId) {
		shoppingCartDAO.deleteAllCartItems(memberId);
	}

	// 장바구니 선택 상품 제거
	public void flush(int memberId, List<Product> selectedProducts) {

		// 선택된 상품이 없는 경우
		if (selectedProducts == null || selectedProducts.isEmpty()) {
			throw new ValidationException(ErrorCode.EMPTY_SELECTED_PRODUCTS);
		}

		for (Product product : selectedProducts) {

			// 상품 정보가 없는 경우
			if (product == null) {
				throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
			}

			int productId = product.getProductId();

			// 장바구니에 존재하지 않는 상품인 경우
			if (!shoppingCartDAO.existsCartItem(memberId, productId)) {
				throw new DataNotFoundException(ErrorCode.CART_PRODUCT_NOT_FOUND);
			}

			shoppingCartDAO.deleteCartItem(memberId, productId);
		}
	}

	// 장바구니 상품 전체 주문
	public Map<Product, Integer> flushByOrder(int memberId) {
		// 기존 장바구니 복사
		Map<Product, Integer> orderedProducts = new HashMap<>(shoppingCartDAO.findCartProductsByMemberId(memberId));

		// 장바구니 비우기
		shoppingCartDAO.deleteAllCartItems(memberId);

		// 주문 상품 반환
		return orderedProducts;
	}

	// 장바구니 선택 상품 주문
	public Map<Product, Integer> flushByOrder(int memberId, List<Product> selectedProducts) {

		// 선택된 상품이 없는 경우
		if (selectedProducts == null || selectedProducts.isEmpty()) {
			throw new ValidationException(ErrorCode.EMPTY_SELECTED_PRODUCTS);
		}

		Map<Product, Integer> orderedProducts = new HashMap<>();

		for (Product product : selectedProducts) {

			// 상품 정보가 없는 경우
			if (product == null) {
				throw new DataNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
			}

			int productId = product.getProductId();

			// 장바구니에 존재하지 않는 상품인 경우
			if (!shoppingCartDAO.existsCartItem(memberId, productId)) {
				throw new DataNotFoundException(ErrorCode.CART_PRODUCT_NOT_FOUND);
			}

			// 주문 상품 저장
			int amount = shoppingCartDAO.findAmount(memberId, productId);
			orderedProducts.put(product, amount);

			// 장바구니에서 제거
			shoppingCartDAO.deleteCartItem(memberId, productId);
		}

		return orderedProducts;
	}
}
