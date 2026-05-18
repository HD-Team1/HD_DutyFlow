package stock.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import product.Product;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class Stock {
	private int stockId;
	private int productId;
	private LocalDate manufacturedDate;
	private int amount;
}
