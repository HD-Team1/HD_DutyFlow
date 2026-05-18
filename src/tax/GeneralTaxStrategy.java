package tax;

import java.math.BigDecimal;

import exception.BusinessException;
import exception.ErrorCode;
import order.Order;
import regulation.RegulationDTO;

public class GeneralTaxStrategy implements TaxStrategy{
	
	private final RegulationDTO regulationDTO;
	
	 public GeneralTaxStrategy(RegulationDTO regulationDTO) {
		 
		 if (regulationDTO == null) {
			 throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
		 }
		 
		 this.regulationDTO = regulationDTO;
	 }
	 

	@Override
	public BigDecimal calculateTax(Order order) {
		
		if(order == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		
		if (order.getTotalPrice() == null) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_PRICE);
        }
		
		if (regulationDTO.getOverageRate() == 0) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATE);
        }
		 
		BigDecimal dutyFreeLimit = BigDecimal.valueOf(regulationDTO.getLimitCapacity());
	     
		BigDecimal taxRate = BigDecimal.valueOf(regulationDTO.getOverageRate()).divide(BigDecimal.valueOf(100));
		
		BigDecimal totalDollarsPrice = order.getTotalPrice();
		BigDecimal exceededAmount = totalDollarsPrice.subtract(dutyFreeLimit);
		
		if (exceededAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		
		return exceededAmount.multiply(taxRate);
	}

}
