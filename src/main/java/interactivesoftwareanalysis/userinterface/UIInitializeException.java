package interactivesoftwareanalysis.userinterface;

/**
 * An exception that is thrown, when the ui could not be initialized
 */
public class UIInitializeException extends RuntimeException {
    public UIInitializeException() {
    }

    public UIInitializeException(String message) {
        super(message);
    }

    public UIInitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UIInitializeException(Throwable cause) {
        super(cause);
    }

    public UIInitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
