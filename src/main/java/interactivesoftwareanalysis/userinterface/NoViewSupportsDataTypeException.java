package interactivesoftwareanalysis.userinterface;

/**
 * An exception that is thrown, when no view exists, that can
 * display an interactive submodules produced data type
 */
public class NoViewSupportsDataTypeException extends RuntimeException {
    public NoViewSupportsDataTypeException() {
    }

    public NoViewSupportsDataTypeException(String message) {
        super(message);
    }

    public NoViewSupportsDataTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoViewSupportsDataTypeException(Throwable cause) {
        super(cause);
    }

    public NoViewSupportsDataTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
