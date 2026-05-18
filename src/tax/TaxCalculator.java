package tax;

import java.math.BigDecimal;
import java.util.List;
import order.Order;
import exception.BusinessException;
import exception.ErrorCode;

public class TaxCalculator {

	private List<TaxStrategy> strategies;
	
	public TaxCalculator(List<TaxStrategy> strategies) {
		
		if (strategies == null || strategies.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
		
		this.strategies = strategies;
	}
	
	public BigDecimal calculateTax(Order order) {
		
		if (order == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
		
		BigDecimal total = BigDecimal.ZERO;
		
		for(TaxStrategy strategy : strategies) {
			total = total.add(strategy.calculateTax(order));
		}

		return total;
	}
	
}
