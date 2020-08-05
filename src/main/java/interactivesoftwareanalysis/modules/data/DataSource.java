package interactivesoftwareanalysis.modules.data;

/**
 * A data source that modules can use to import data by executing commands.
 */
public interface DataSource {

    /**
     * Execute a command on this data source
     * @param command the command to execute. The exact meaning of this may change depending on the type of data source.
     * @return the result of the command execution
     * @throws DataSourceExecuteException when the command could not be executed successfully by the data source
     */
    String execute(String command) throws DataSourceExecuteException;
}
