package tax;

import java.math.BigDecimal;
import order.Order;
import regulation.RegulationDTO;
import exception.BusinessException;
import exception.ErrorCode;

public class PerfumeTaxStrategy implements TaxStrategy{

	private final RegulationDTO regulationDTO;

	public PerfumeTaxStrategy(RegulationDTO regulationDTO) {
		
		if (regulationDTO == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
		
	    this.regulationDTO = regulationDTO;
	 }

	@Override
    public BigDecimal calculateTax(Order order) {
		
		if (order == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (regulationDTO.getOverageRate() == 0) {
            throw new BusinessException(ErrorCode.ILLEGAL_STATE);
        }
		
        int limitCapacity = regulationDTO.getLimitCapacity();
        
        BigDecimal taxRate = BigDecimal.valueOf(regulationDTO.getOverageRate())
                                      .divide(BigDecimal.valueOf(100));

        int totalPerfume = order.getTotalPerfume();
        int exceeded = totalPerfume - limitCapacity;

        if (exceeded <= 0) {
        	return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(exceeded).multiply(taxRate);
    }

}
