package flight;

import java.time.LocalDateTime;
import java.util.List;

import airplane.Airplane;

import exception.DataNotFoundException;
import exception.ErrorCode;
import exception.ValidationException;
import exception.BusinessException;
import lombok.NoArgsConstructor;


public class FlightService {

    private final FlightDAO flightDAO;

    //비행기 예약 번호의 정규식
    private static final String RESV_CODE_REGEX = "^RESV-[A-Z]{2}\\d{3}-\\d{3}$";
    
    public FlightService(FlightDAO flightDAO) {
        this.flightDAO = flightDAO;
    }
    
    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    }
 

    public FlightService() {
		this.flightDAO = new FlightDAO();
    }
    // 항공편 조회 (예약코드 기반)
    public Airplane getFlightInfo(String reservationCode) {
    	
    	validateCode(reservationCode);

        FlightDTO flightDto = flightDAO.getFlightByReservationCode(reservationCode);

        if (flightDto == null) {
            throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND);
        }

        return new Airplane(
        		flightDto.getFlightId(),
        		flightDto.getFlightCode(),
        		flightDto.getDepartureAt()
        );
    }
    
  
    // 예약 정보 조회 (회원Id, 항공편 코드 기반)
    public FlightBookDTO getBookByMemberAndFlight(int memberId, String flightCode) {

    	validateCode(flightCode);
    	
    	if (memberId <= 0) {
            throw new ValidationException(ErrorCode.INVALID_INPUT);
        }
    	
        FlightBookDTO flightBookDto = flightDAO.getBookByMemberAndFlight(memberId, flightCode);

        if (flightBookDto == null) {
            throw new DataNotFoundException(ErrorCode.DATA_NOT_FOUND);
        }

        return flightBookDto;
    }
    


    // 항공편 지연 정보 업데이트 
    // 항공편 조회 (memberid 기반)
    public FlightBookDTO getFlightBookByMemberId(int memberId) {


        List<FlightBookDTO> list =
        		flightDAO.getFlightBookByMemberId(memberId);

        if (list == null || list.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DATA_NOT_FOUND
            );
        }

        return list.get(0);
    }
    
    
    // 지연 정보 처리
    public void updateDelayedFlight(String flightCode, LocalDateTime newDepartureAt) {

        if (flightCode == null || newDepartureAt == null) {
        	throw new ValidationException(ErrorCode.INVALID_INPUT);
        }

        flightDAO.updateDelayedFlight(flightCode, newDepartureAt);
    }
    
    /**
     * 예약 코드 유효성 검증 (정규식 + DB 존재 여부)
     */
    public void validateReservationCode(String reservationCode) {
        // 1. 형식 검증 (정규식)
        if (reservationCode == null || !reservationCode.matches(RESV_CODE_REGEX)) {
            System.out.println("[FlightService] 유효하지 않은 예약 코드 형식: " + reservationCode);
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 2. DB 존재 여부 검증
        FlightDTO flight = flightDAO.getFlightByReservationCode(reservationCode);
        if (flight == null) {
            System.out.println("[FlightService] 존재하지 않는 예약 코드: " + reservationCode);
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND); 
        }
    }
    
}
