package admin.airportmanager;

import java.time.LocalDate;
import java.util.List;

import admin.airportmanager.dto.AirportManagerLoginDto;
import admin.airportmanager.dto.PickUpListDTO;
import exception.BusinessException;
import exception.ErrorCode;
import member.Member;

public class AirportManagerService {
	
	private final AirportManagerDao airportManagerDao;
	private AirportManager airportManagerSession;
	
	private AirportManagerLoginDto airportManagerLoginDto;
	 
	public AirportManagerService(AirportManagerDao airportManagerDao) {
		this.airportManagerDao = airportManagerDao;
	}
	
	// 로그인 
	public void login(int managerId, String password) {
		 
		if (this.airportManagerSession != null) { 
	        throw new BusinessException(ErrorCode.ALREADY_LOGGED_IN,
	            new Exception("이미 로그인된 상태입니다. 현재 관리자: " + airportManagerSession.getManagerName()));
	    }
 
		AirportManager manager = airportManagerDao.findById(managerId);

		if (manager == null || !manager.authenticate(password)) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIAL,
				new Exception("관리자 ID 또는 비밀번호가 올바르지 않습니다."));
		}
 
		this.airportManagerSession = manager;
	}
	
	// 로그아웃 
	public void logout() {
		checkLoggedIn(); 
	    this.airportManagerSession = null; // 세션 초기화
	}
	
	 // 전체 픽업 목록 처리
	public List<PickUpListDTO> getAllPickUpList() {
        checkLoggedIn();
        return airportManagerDao.getAllPickUpList();
    }
	
	 // 특정 회원 픽업 목록 출력
	 public List<PickUpListDTO> getAllPickUpListByMember(Member member) {
	        checkLoggedIn();
	 
	        if (member == null) {
	            throw new BusinessException(ErrorCode.INVALID_INPUT,
	                new Exception("Member 객체가 null입니다."));
	        }
	 
	        return airportManagerDao.getAllPickUpListByMember(member);
	    }
	
	// 기간별 픽업 목록 출력
	 public List<PickUpListDTO> getAllPickUpListByDateRange(LocalDate start, LocalDate end) {
	        checkLoggedIn();
	 
	        if (start == null || end == null) {
	            throw new BusinessException(ErrorCode.INVALID_INPUT,
	                new Exception("start 또는 end 날짜가 null입니다."));
	        }
	        if (start.isAfter(end)) {
	            throw new BusinessException(ErrorCode.INVALID_INPUT,
	                new Exception("start 날짜가 end 날짜보다 늦을 수 없습니다."));
	        }
	 
	        return airportManagerDao.getAllPickUpListByDateRange(start, end);
	    }

	 private void checkLoggedIn() {
		 if (this.airportManagerSession == null) { 
		        throw new BusinessException(ErrorCode.NOT_LOGGED_IN,
		            new Exception("로그인 후 이용 가능합니다."));
		    }
    }

	

}
