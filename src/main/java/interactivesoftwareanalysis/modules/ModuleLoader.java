package interactivesoftwareanalysis.modules;

import java.util.List;

/**
 * A loader for modules
 */
@FunctionalInterface interface ModuleLoader {

    /**
     * Load and instantiate modules.
     * The implementation can decide, how to load modules and where to get them from.
     * @param moduleContext the module context to instantiate the modules with
     * @return a list of modules tha were loaded or an empty list.
     */
    List<Module> loadModules(ModuleContext moduleContext);
}
