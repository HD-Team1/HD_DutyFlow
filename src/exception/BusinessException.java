package exception;

public class BusinessException extends DutyFreeException {

    public BusinessException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}