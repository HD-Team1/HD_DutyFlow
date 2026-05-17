package gui.fakedata;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FakeMemberStore {

    private static final List<FakeProduct> products = new ArrayList<>();
    private static final List<FakeCartItem> cartItems = new ArrayList<>();
    private static final List<FakeOrder> orders = new ArrayList<>();

    static {
        products.add(new FakeProduct(
                1,
                "조니워커 블루라벨",
                "Johnnie Walker",
                "주류",
                750,
                new BigDecimal("220.00"),
                new BigDecimal("298000"),
                new BigDecimal("5"),
                19,
                true,
                "부드러운 풍미와 깊은 향을 가진 프리미엄 위스키입니다."
        ));

        products.add(new FakeProduct(
                2,
                "샤넬 No.5",
                "CHANEL",
                "향수",
                100,
                new BigDecimal("145.00"),
                new BigDecimal("196000"),
                new BigDecimal("0"),
                8,
                false,
                "클래식한 플로럴 향을 가진 대표적인 여성 향수입니다."
        ));

        products.add(new FakeProduct(
                3,
                "디올 립글로우",
                "DIOR",
                "화장품",
                4,
                new BigDecimal("38.00"),
                new BigDecimal("51000"),
                new BigDecimal("10"),
                25,
                true,
                "자연스러운 혈색을 살려주는 인기 립밤 제품입니다."
        ));

        products.add(new FakeProduct(
                4,
                "정관장 홍삼정",
                "정관장",
                "건강식품",
                240,
                new BigDecimal("95.00"),
                new BigDecimal("128000"),
                new BigDecimal("0"),
                0,
                false,
                "면역력 관리에 도움을 주는 대표 홍삼 제품입니다."
        ));

        products.add(new FakeProduct(
                5,
                "발렌타인 21년",
                "Ballantine's",
                "주류",
                700,
                new BigDecimal("180.00"),
                new BigDecimal("244000"),
                new BigDecimal("7"),
                12,
                true,
                "깊고 균형 잡힌 향을 가진 인기 위스키 상품입니다."
        ));
    }

    public static List<FakeProduct> getProducts() {
        return products;
    }

    public static List<FakeCartItem> getCartItems() {
        return cartItems;
    }

    public static List<FakeOrder> getOrders() {
        return orders;
    }

    public static void addToCart(FakeProduct product, int quantity) {
        for (FakeCartItem item : cartItems) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.addQuantity(quantity);
                return;
            }
        }

        cartItems.add(new FakeCartItem(product, quantity));
    }

    public static void removeCartItem(int productId) {
        cartItems.removeIf(item -> item.getProduct().getProductId() == productId);
    }

    public static void clearCart() {
        cartItems.clear();
    }

    public static BigDecimal getTotalUsd() {
        BigDecimal total = BigDecimal.ZERO;

        for (FakeCartItem item : cartItems) {
            total = total.add(item.getTotalUsd());
        }

        return total;
    }

    public static BigDecimal getTotalKrw() {
        BigDecimal total = BigDecimal.ZERO;

        for (FakeCartItem item : cartItems) {
            total = total.add(item.getTotalKrw());
        }

        return total;
    }

    public static int getTotalQuantity() {
        int total = 0;

        for (FakeCartItem item : cartItems) {
            total += item.getQuantity();
        }

        return total;
    }

    public static void createOrderFromCart() {
        if (cartItems.isEmpty()) {
            return;
        }

        List<FakeCartItem> copiedItems = new ArrayList<>();

        for (FakeCartItem item : cartItems) {
            copiedItems.add(new FakeCartItem(item.getProduct(), item.getQuantity()));
        }

        FakeOrder order = new FakeOrder(
                orders.size() + 1,
                LocalDateTime.now(),
                copiedItems,
                getTotalUsd(),
                getTotalKrw(),
                "PAID"
        );

        orders.add(order);
        clearCart();
    }
}