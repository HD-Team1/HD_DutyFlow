package exception;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import common.OracleConnection;

public class SystemLogDAO {

	public void save(String errorCode, String logMessage, String detailMessage) {
        
        String sql = "INSERT INTO SystemLog (errorCode, logMessage, detailLogMessage, createdAt) VALUES (?, ?, ?, SYSDATE)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, errorCode);
            
            String summary = (logMessage != null && logMessage.length() > 512) 
                             ? logMessage.substring(0, 509) + "..." 
                             : logMessage;
            
            pstmt.setString(2, summary);
            
            pstmt.setString(3, detailMessage);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[SystemLog] DB 저장 실패: " + e.getMessage());
        }
       
    }
}