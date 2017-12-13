package polibee.core.background;

import polibee.core.PolibeeException;

/**
 * Exception liên quan đến Event
 */
public class EventException extends PolibeeException {
    public EventException(String message) {
        super(message);
    }

    public EventException(Exception innerException) {
        super(innerException);
    }

    public EventException(String message, Exception innerException) {
        super(message, innerException);
    }
}
