package shoppingCart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class TotalCartDTO {

    private final List<CartItemDTO> items;
    private final int totalQuantity;
    private final BigDecimal totalDollarPrice;
    private final BigDecimal totalWonPrice;
}