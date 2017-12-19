package polibee.core.background;

import polibee.core.PolibeeException;

/**
 * Exception liên quan hệ thống Background Job
 */
public class JobException extends PolibeeException {
    public JobException(String message) {
        super(message);
    }

    public JobException(String message, Exception innerEx) {
        super(message, innerEx);
    }
}
