package stock.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import category.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@Builder
@ToString
public class StockProductDto {
	private Category category;
	private String productName;
	private int capacity;
	private BigDecimal priceUsd;
	private BigDecimal priceKrw;
	private double discountRate;
	private int thresholdValue;
	private LocalDate manufacturedDate;
	private int amount;
}
