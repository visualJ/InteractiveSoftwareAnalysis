package interactivesoftwareanalysis.modules.data;

import interactivesoftwareanalysis.modules.Describable;
import javafx.scene.Node;

/**
 * A provider for a data source. It can provide a UI for entering parameters that
 * are necessary for creating the data source. It can also crete new instances of the data source type.
 */
public interface DataSourceProvider extends Describable {

    /**
     * Retrieve a new data source instance. It can be parametrized using the ui, if necessary
     * @return anew data source instance
     * @throws DataSourceCreateException when the data source can not be created
     */
    DataSource getDataSource() throws DataSourceCreateException;

    /**
     * Retrieve the ui that can be used to parametrize the creation of the data source
     * @return a ui node
     */
    Node getUI();
}
