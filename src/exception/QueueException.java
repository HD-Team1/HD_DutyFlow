package exception;

public class QueueException extends DutyFreeException {

    public QueueException(ErrorCode errorCode, Exception logMessage) {
        super(errorCode, logMessage);
    }
    
    public QueueException(ErrorCode errorCode) {
        super(errorCode);
    }
}