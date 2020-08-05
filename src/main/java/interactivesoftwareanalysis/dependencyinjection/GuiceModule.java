package interactivesoftwareanalysis.dependencyinjection;

import com.gluonhq.ignite.DIContext;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import interactivesoftwareanalysis.model.Model;
import interactivesoftwareanalysis.model.JenaRDFModel;
import interactivesoftwareanalysis.modules.data.DataSourceManager;
import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.userinterface.UIManager;

import javax.inject.Singleton;

/**
 * This module defines the dependency injection bindings in this project.
 * There are only a few bound classes and interfaces, so they all fit into one module at this point.
 */
public class GuiceModule extends AbstractModule {
    private DIContext diContext;

    @Override protected void configure() {
        bind(Model.class).to(JenaRDFModel.class).in(Singleton.class);
        bind(UIManager.class).in(Singleton.class);
        bind(ModuleManager.class).in(Singleton.class);
        bind(EventBus.class).in(Singleton.class);
        bind(ModuleContext.class).in(Singleton.class);
        bind(DataSourceManager.class).in(Singleton.class);
        bind(DIContext.class).toInstance(diContext);
    }

    /**
     * Add the dependency injection context to said context, so that the
     * context can inject itself. Injected instances can use this to create other injected instances at runtime.
     * @param diContext the dependency injection context to make available in this DI context.
     */
    public void addDIContext(DIContext diContext){
        this.diContext = diContext;
    }
}
