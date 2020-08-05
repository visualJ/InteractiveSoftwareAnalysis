package interactivesoftwareanalysis.modules.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A base class for data source providers
 */
@RequiredArgsConstructor public abstract class DataSourceProviderBase implements DataSourceProvider {

    @Getter protected final String name;
    @Getter protected final String description;

    @Override
    public String toString() {
        return name;
    }
}
