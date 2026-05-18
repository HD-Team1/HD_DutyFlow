package exception;

public class AuthenticationException extends DutyFreeException {

    public AuthenticationException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}