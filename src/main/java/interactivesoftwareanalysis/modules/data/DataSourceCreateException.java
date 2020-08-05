package interactivesoftwareanalysis.modules.data;

/**
 * An exception that is thrown when a data source could not be created.
 */
public class DataSourceCreateException extends Exception {
    public DataSourceCreateException() {
    }

    public DataSourceCreateException(String message) {
        super(message);
    }

    public DataSourceCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceCreateException(Throwable cause) {
        super(cause);
    }

    public DataSourceCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
