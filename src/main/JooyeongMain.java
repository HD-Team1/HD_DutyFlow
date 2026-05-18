package main;

import java.math.BigDecimal;
import java.util.List;

import exchangeRate.ExchangeRateService;
import product.Product;
import shoppingCart.ShoppingCartService;
import shoppingCart.dto.TotalCartDTO;

public class JooyeongMain {

    public static void main(String[] args) {
    	
        ExchangeRateService exchangeRateService = new ExchangeRateService();

        exchangeRateService.updateDailyExchangeRate();

        System.out.println("환율 갱신 완료");

        ShoppingCartService service = new ShoppingCartService();

        int memberId = 1;   // DML로 넣어둔 테스트 회원
        int productId = 1;  // DML로 넣어둔 테스트 상품

        Product product = Product.builder()
                .productId(productId)
                .productName("샤넬 향수")
                .priceUsd(new BigDecimal("120"))
                .priceKrw(new BigDecimal("160000"))
                .build();

        // =========================
        // 1. 장바구니 전체 비우기 테스트
        // =========================
        System.out.println("===== 장바구니 전체 비우기 =====");

        try {
            service.flush(memberId);
            System.out.println(service.getCart(memberId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 2. 장바구니 상품 추가 테스트
        // =========================
        System.out.println("===== 장바구니 상품 추가 =====");

        try {
            service.addToCart(memberId, product, 2);
            TotalCartDTO cart = service.getCart(memberId);
            System.out.println(cart);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 3. 같은 상품 추가 테스트
        // =========================
        System.out.println("===== 같은 상품 추가 =====");

        try {
            service.addToCart(memberId, product, 3);
            TotalCartDTO cart = service.getCart(memberId);
            System.out.println(cart);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 4. 장바구니 상품 수량 변경 테스트
        // =========================
        System.out.println("===== 장바구니 상품 수량 변경 =====");

        try {
            service.updateQuantity(memberId, product, 10);
            TotalCartDTO cart = service.getCart(memberId);
            System.out.println(cart);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 5. 장바구니 선택 상품 삭제 테스트
        // =========================
        System.out.println("===== 장바구니 선택 상품 삭제 =====");

        try {
            service.flush(memberId, List.of(product));
            TotalCartDTO cart = service.getCart(memberId);
            System.out.println(cart);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 6. 예외 테스트 - 없는 상품 삭제
        // =========================
        System.out.println("===== 예외 테스트: 없는 상품 삭제 =====");

        try {
            service.flush(memberId, List.of(product));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 7. 예외 테스트 - 수량 0 이하
        // =========================
        System.out.println("===== 예외 테스트: 수량 0 이하 =====");

        try {
            service.addToCart(memberId, product, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // 8. 예외 테스트 - 상품 null
        // =========================
        System.out.println("===== 예외 테스트: 상품 null =====");

        try {
            service.addToCart(memberId, null, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}