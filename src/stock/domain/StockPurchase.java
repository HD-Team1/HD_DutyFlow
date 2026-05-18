package stock.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import product.Product;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPurchase {
    private int purchaseId;
    private int productId;
    private LocalDateTime purchaseDate;
    private int amount;
    private StockPurchaseStatus status;
}
