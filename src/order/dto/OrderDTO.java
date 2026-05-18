package order.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import order.OrderState;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

	// category
	private int categoryId;
	private String categoryName;
	private int capacity;

	// Orders
	private int orderId;
	private int memberId;
	private int reservationId;
	private LocalDate exchangeDate;
	private LocalDateTime orderedAt;
	private String orderState;
	private BigDecimal totalAmount;

	// OrderDetail
	private int productId;
	private int quantity;
	private BigDecimal discountPrice;
	private BigDecimal dollarPrice;

	// Product
	private String productName;
	private String brandName;
	
	public BigDecimal getDiscountedUnitPrice() {
	    // discountPrice 필드에 20, 30 같은 할인율이 들어있는 경우
		BigDecimal rate = discountPrice.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
	    BigDecimal discountAmt = this.dollarPrice.multiply(rate);
	    return this.dollarPrice.subtract(discountAmt);
	}

	public BigDecimal getTotalLinePrice() {
	    return getDiscountedUnitPrice().multiply(BigDecimal.valueOf(this.quantity));
	}
	
}
