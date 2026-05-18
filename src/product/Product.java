package product;

import java.math.BigDecimal;
import java.time.LocalDate;

import category.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
	private int productId;
	private Category category;
	private String productName;
	private String brandName;
	private int capacity;
	private BigDecimal priceUsd;
	private BigDecimal priceKrw;
	private int discountRate;
	private int thresholdValue;
 
}