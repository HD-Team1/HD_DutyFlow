package exception;

public class DataNotFoundException extends DutyFreeException {

    public DataNotFoundException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
