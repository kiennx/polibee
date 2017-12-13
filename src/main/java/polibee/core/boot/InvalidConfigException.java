package polibee.core.boot;

import polibee.core.PolibeeException;

/**
 * Exception khi cấu hình ứng dụng không hợp lệ
 */
public class InvalidConfigException extends PolibeeException {
    public InvalidConfigException(String message) {
        super(message);
    }

    public InvalidConfigException(Exception innerException) {
        super(innerException);
    }

    public InvalidConfigException(String message, Exception innerException) {
        super(message, innerException);
    }
}