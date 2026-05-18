package exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DutyFreeException extends RuntimeException {

    private final ErrorCode errorCode;
    private static final SystemLogDAO systemLogDAO = new SystemLogDAO();

    public DutyFreeException(ErrorCode errorCode, Exception message) {
        super(message);
        this.errorCode = errorCode;
        
        String detailStack = getStackTraceAsString(message);
        
        systemLogDAO.save(
            errorCode.getCode(),
            message != null ? message.getMessage() : errorCode.getMessage(),
            detailStack 
        );
    }
    
    public DutyFreeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        
        systemLogDAO.save(
            errorCode.getCode(),
            errorCode.getMessage(),
            "상세 스택 정보 없음"
        );
    }

    /**
     * Exception 객체로부터 전체 스택 트레이스를 추출하여 문자열로 반환합니다.
     */
    private String getStackTraceAsString(Exception e) {
        if (e == null) return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); 
        return sw.toString(); 
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}