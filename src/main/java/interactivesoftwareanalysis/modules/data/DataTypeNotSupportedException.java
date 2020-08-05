package interactivesoftwareanalysis.modules.data;

/**
 * Exception thrown when a module does not support a requested data type
 */
public class DataTypeNotSupportedException extends RuntimeException {
    public DataTypeNotSupportedException() {
    }

    public DataTypeNotSupportedException(String message) {
        super(message);
    }

    public DataTypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataTypeNotSupportedException(Throwable cause) {
        super(cause);
    }

    public DataTypeNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
