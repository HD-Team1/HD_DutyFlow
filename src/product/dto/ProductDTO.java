package product.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import category.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	
	private int productId;
	private Category category;
	private String productName;
	private String brandName;
	private int capacity;
	private BigDecimal priceUsd;
	private BigDecimal priceKrw;
	private double discountRate;
	private int thresholdValue;
	private boolean hasEvent; // 상품 할인 유무
	private BigDecimal finalPriceKrw; // 화면에 뿌릴것이므로 DTO에 추가
	private BigDecimal finalPriceUsd; // 화면에 뿌릴것이므로 DTO에 추가
	
  
}