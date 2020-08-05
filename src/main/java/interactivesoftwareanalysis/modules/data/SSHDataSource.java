package interactivesoftwareanalysis.modules.data;

import com.jcabi.ssh.SSHByPassword;
import com.jcabi.ssh.Shell;
import interactivesoftwareanalysis.modules.data.DataSource;
import interactivesoftwareanalysis.modules.data.DataSourceCreateException;
import interactivesoftwareanalysis.modules.data.DataSourceExecuteException;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A data source, that executes commands by sending them to a remote computer
 */
public class SSHDataSource implements DataSource {

    private Shell.Plain shell;

    public SSHDataSource(String host, int port, String username, String password) throws UnknownHostException, DataSourceCreateException {

        // create a new shell for sending commands to the host
        shell = new Shell.Plain(new SSHByPassword(host, port, username, password));

        // execute an empty command to ckeck, if the host answers
        try {
            shell.exec("");
        } catch (IOException e) {
            throw new DataSourceCreateException("Es konnte keine Verbindung mit dem Host hergestellt werden.\n\nFehlermeldung:\n" + e.getMessage(), e);
        }
    }

    @Override public String execute(String command) throws DataSourceExecuteException {
        try {
            return shell.exec(command);
        } catch (IOException e) {
            throw new DataSourceExecuteException(e);
        }
    }

}
