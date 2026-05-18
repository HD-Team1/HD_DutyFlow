package exception;

public class ValidationException extends DutyFreeException{
	
	public ValidationException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
	
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
