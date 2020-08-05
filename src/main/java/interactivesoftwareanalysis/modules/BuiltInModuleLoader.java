package interactivesoftwareanalysis.modules;

import java.util.Arrays;
import java.util.List;

/**
 * Instantiates and provides built in modules
 */
public class BuiltInModuleLoader implements ModuleLoader {
    @Override
    public List<Module> loadModules(ModuleContext moduleContext) {
        return Arrays.asList(
                new FileSystemModule(moduleContext),
                new GeneralModule(moduleContext),
                new PackageModule(moduleContext)
        );
    }
}
