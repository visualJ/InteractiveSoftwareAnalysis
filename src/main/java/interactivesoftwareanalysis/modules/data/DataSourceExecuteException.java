package interactivesoftwareanalysis.modules.data;

/**
 * An exception that is thrown when a command could not be executed successfully by a data source
 */
public class DataSourceExecuteException extends Exception{
    public DataSourceExecuteException() {
    }

    public DataSourceExecuteException(String message) {
        super(message);
    }

    public DataSourceExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceExecuteException(Throwable cause) {
        super(cause);
    }

    public DataSourceExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
