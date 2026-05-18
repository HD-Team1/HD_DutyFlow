package stock.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import stock.domain.StockPurchaseStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPurchaseHistoryDto {

    private int purchaseId;
    private int productId;

    private String productName;
    private String brandName;
    private String categoryName;
    
	private BigDecimal priceUsd;
	private BigDecimal priceKrw;
	private int thresholdValue;
	
    private LocalDateTime purchaseDate;
    
    private int amount;
    private StockPurchaseStatus status;
}