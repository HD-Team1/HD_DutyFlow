package shoppingCart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShoppingCart {
    private int memberId;
    private int productId;
    private int amount;
}
