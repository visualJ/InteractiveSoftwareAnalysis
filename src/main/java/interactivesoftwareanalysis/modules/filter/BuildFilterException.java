package interactivesoftwareanalysis.modules.filter;

/**
 * An exception that is thrown when building a filter failed
 */
public class BuildFilterException extends Exception {
    public BuildFilterException() {
    }

    public BuildFilterException(String message) {
        super(message);
    }

    public BuildFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuildFilterException(Throwable cause) {
        super(cause);
    }

    public BuildFilterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
