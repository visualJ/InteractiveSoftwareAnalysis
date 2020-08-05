package interactivesoftwareanalysis.modules.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

/**
 * A manage for data sources. It manages one active data source and can retrieve informaiton about
 * available data sources.
 * New data sourcs have to be made available here by adding data sources providers.
 */
public class DataSourceManager {
    @Getter @Setter private DataSource dataSource;
    @Getter private ObservableList<DataSourceProvider> dataSourceProviders;

    public DataSourceManager(){
        this.dataSourceProviders = FXCollections.observableArrayList(new SSHDataSourceProvider());
    }
}
