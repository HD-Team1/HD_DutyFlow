package member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Grade;
import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;


public class MemberDAO {

	public Member findById(int memberId) {
	    String sql = "SELECT memberId, loginId, name, grade FROM Member WHERE memberId = ?";
	    // (수정) 처음에는 null로 초기화
	    Member member = null;

	    try (Connection conn = OracleConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, memberId);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                member = new Member(); // (수정) 결과가 있을 때만 객체 생성
	                member.setMemberId(rs.getInt("memberId"));
	                member.setLoginId(rs.getString("loginId"));
	                member.setName(rs.getString("name"));
	                
	                String gradeStr = rs.getString("grade");
	                if (gradeStr != null) {
	                    member.setGrade(Grade.valueOf(gradeStr.toUpperCase()));
	                }
	            }
	        }
	    } catch (SQLException e) {
	        throw new SystemException(ErrorCode.DB_CONNECTION);
	    }
	    return member; // 결과가 없으면 null 리턴
	}
	
	// 아이디 중복 확인
    public boolean existsByLoginId(String loginId) {
        String sql = "SELECT COUNT(*) FROM Member WHERE loginId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginId);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0; // 존재할 경우 true 반환
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    // 전화번호 중복 확인
    public boolean existsByPhoneNumber(String phoneNumber) {
        String sql = "SELECT COUNT(*) FROM Member WHERE phoneNumber = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phoneNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }

    public void insert(Member member) {
        String sql =
            "INSERT INTO Member (grade, loginId, password, name, birthDate, "
            + "phoneNumber, gradeSelectionDate, adult) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getGrade().name());
            pstmt.setString(2, member.getLoginId());
            pstmt.setString(3, member.getPassword());
            pstmt.setString(4, member.getName());
            pstmt.setDate(5, java.sql.Date.valueOf(member.getBirthDate()));
            pstmt.setString(6, member.getPhoneNumber());
            pstmt.setDate(7, java.sql.Date.valueOf(member.getGradeSelectionDate()));
            pstmt.setString(8, member.isAdult() ? "Y" : "N");

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 여권번호 중복 확인
    public boolean existsByPassportNum(String passportNum) {
        String sql = "SELECT COUNT(*) FROM Member WHERE passportNumber = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passportNum);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 회원의 여권번호 및 여권 만료일 정보를 업데이트
    public void updatePassport(int memberId, String passportNum, LocalDate passportExpiredDate) {
        String sql = "UPDATE Member SET passportNumber = ?, passportExpiryDate = ? WHERE memberId = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, passportNum);
            pstmt.setDate(2, java.sql.Date.valueOf(passportExpiredDate));
            pstmt.setInt(3, memberId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
    
    // 아이디, 비밀번호로 회원 조회
    public Member findByLoginIdAndPassword(String loginId, String password) {

    	String sql = "SELECT memberId, loginId, password, name, adult, grade, passportExpiryDate FROM Member WHERE loginId = ? AND password = ?";
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                	return Member.builder()
                	        .memberId(rs.getInt("memberId"))
                	        .loginId(rs.getString("loginId"))
                	        .password(rs.getString("password"))
                	        .name(rs.getString("name"))
                	        .isAdult("Y".equals(rs.getString("adult")))
                	        .grade(Grade.valueOf(rs.getString("grade")))
                	        .passportExpiredDate(
                	                rs.getDate("passportExpiryDate") == null
                	                        ? null
                	                        : rs.getDate("passportExpiryDate").toLocalDate()
                	        )
                	        .build();
                }
            }

            return null;

        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION, e);
        }
    }
}
