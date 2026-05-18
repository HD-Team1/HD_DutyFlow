package exception;

public class SystemException extends DutyFreeException {

    public SystemException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
}