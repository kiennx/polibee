package polibee.core;

/**
 * Lớp định nghĩa chung cho các Exception của hệ thống sử dụng nền Polibee
 */
public class PolibeeException extends Exception {
    public PolibeeException(String message) {
        super(message);
    }

    public PolibeeException(Exception innerException) {
        super(innerException);
    }

    public PolibeeException(String message, Exception innerException) {
        super(message, innerException);
    }
}
