package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import exception.ErrorCode;
import exception.SystemException;


public class OracleConnection {

    private static final String URL =
//             "jdbc:oracle:thin:@localhost:1521/XEPDB1";
            "jdbc:oracle:thin:@192.168.2.168:1521/XEPDB1";
//			  "jdbc:oracle:thin:@//localhost:1521/freepdb1";

    private static final String USER = "DUTY_MANAGER";
    private static final String PASSWORD = "1004";

    static {
    	
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SystemException(ErrorCode.DB_DRIVER_NOT_FOUND, e);
        }
    }

    public static Connection getConnection() throws SystemException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new SystemException(ErrorCode.DB_CONNECTION,e);
        }
    }
    
}

