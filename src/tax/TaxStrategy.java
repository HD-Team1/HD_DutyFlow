package tax;

import java.math.BigDecimal;

import order.Order;

public interface TaxStrategy {
	BigDecimal calculateTax(Order order);

}
