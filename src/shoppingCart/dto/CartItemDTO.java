package shoppingCart.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemDTO {
    private final int productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal dollarPrice;
    private final BigDecimal wonPrice;
    private final int categoryId;
    private final int capacity;
}