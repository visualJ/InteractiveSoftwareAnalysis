package interactivesoftwareanalysis.modules.data;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Provides the ssh data source type
 */
public class SSHDataSourceProvider extends DataSourceProviderBase {

    @Getter(value = AccessLevel.PRIVATE, lazy = true) private final SSHDataSourceProviderUI providerUI = new SSHDataSourceProviderUI();

    /**
     * UI for entering the necessary parameters for the data source instantiation
     */
    private class SSHDataSourceProviderUI extends VBox {

        @FXML private TextField sshHost;
        @FXML private TextField sshPort;
        @FXML private TextField sshUsername;
        @FXML private PasswordField sshPassword;

        @SneakyThrows(IOException.class)
        public SSHDataSourceProviderUI() {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/ssh_data_source_provider.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        }

        /**
         * Determine whether any of the input fields are empty
         * @return true, iff at least one input field is empty
         */
        private boolean anySSHFieldEmpty(){
        return sshHost.getText().isEmpty() || sshPort.getText().isEmpty() || sshUsername.getText().isEmpty() || sshPassword.getText().isEmpty();
        }

        private String getHost() {
            return sshHost.getText();
        }

        private String getPort() {
            return sshPort.getText();
        }

        private String getUsername() {
            return sshUsername.getText();
        }

        private String getPassword() {
            return sshPassword.getText();
        }
    }

    public SSHDataSourceProvider() {
        super("SSH", "Importiert Daten über eine SSH Verbindung von einem anderen Rechner.");
    }

    @Override
    public DataSource getDataSource() throws DataSourceCreateException {
        SSHDataSourceProviderUI ui = getProviderUI();
        if (ui.anySSHFieldEmpty()) {
            throw new DataSourceCreateException("Es wurden noch nicht alle benötigten Felder ausgefüllt.");
        }
        try {
            // create and return the data source
            return new SSHDataSource(ui.getHost(), Integer.valueOf(ui.getPort()), ui.getUsername(), ui.getPassword());
        } catch (NumberFormatException e) {
            throw new DataSourceCreateException("Als Port muss eine Zahl angegeben werden.", e);
        }catch (UnknownHostException e) {
            throw new DataSourceCreateException("Die IP Adresse des angegebenen Hosts konnte nicht ermittelt werden.", e);
        }
    }

    @Override
    public Node getUI() {
        return getProviderUI();
    }

}
