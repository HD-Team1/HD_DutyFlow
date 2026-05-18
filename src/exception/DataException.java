package exception;

public class DataException extends DutyFreeException {

    public DataException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public DataException(ErrorCode errorCode) {
        super(errorCode);
    }
}