package interactivesoftwareanalysis.modules;

import com.google.common.eventbus.EventBus;
import interactivesoftwareanalysis.model.Model;
import interactivesoftwareanalysis.modules.data.DataSourceManager;
import lombok.Data;

import javax.inject.Inject;

/**
 * A context that contains everything a module needs.
 */
@Data public class ModuleContext {
    private final Model model;
    private final EventBus eventBus;
    private final DataSourceManager dataSourceManager;

    @Inject public ModuleContext(Model model, EventBus eventBus, DataSourceManager dataSourceManager) {
        this.model = model;
        this.eventBus = eventBus;
        this.dataSourceManager = dataSourceManager;
    }
}
