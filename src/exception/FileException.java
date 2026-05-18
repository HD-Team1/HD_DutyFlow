package exception;

public class FileException extends DutyFreeException {

    public FileException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public FileException(ErrorCode errorCode) {
        super(errorCode);
    }
}