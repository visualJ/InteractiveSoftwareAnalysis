package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.filter.*;
import lombok.Getter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by benedikt.ringlein on 23.08.2016.
 */
public class ModuleManager {

    @Getter private List<Module> modules;

    @Inject public ModuleManager(ModuleContext moduleContext) {
        List<ModuleLoader> moduleLoaders = Arrays.asList(
                new BuiltInModuleLoader(),
                new BytecodeModuleLoader()
        );
        modules = moduleLoaders.stream()
                .flatMap(moduleLoader -> moduleLoader.loadModules(moduleContext).stream())
                .collect(Collectors.toList());
    }

    public List<InteractiveSubmodule> getInteractiveSubmodules(){
        return modules.stream().flatMap(module -> module.getInteractiveSubmodules().stream()).collect(Collectors.toList());
    }

    public List<ImportSubmodule> getImportSubmodules(){
        return modules.stream().flatMap(module -> module.getImportSubmodules().stream()).collect(Collectors.toList());
    }

    public List<ExportSubmodule> getExportSubmodules(){
        return modules.stream().flatMap(module -> module.getExportSubmodules().stream()).collect(Collectors.toList());
    }

    public Map<String, DecideFilterFactory> getFilterFactories(){
        return modules.stream().flatMap(module -> module.getFilterFactories().stream()).collect(Collectors.toMap(DecideFilterFactory::getName, filter -> filter));
    }

    public Map<String, CombineFilterFactory> getCombineFilterFactories(){
        HashMap<String, CombineFilterFactory> filters = new HashMap<>();
        filters.put("und", new CombineFilterFactoryBase("und", "") {
            @Override
            public Filter newInstance(List<Filter> filters) {
                return new AndFilter(filters);
            }
        });
        filters.put("oder", new CombineFilterFactoryBase("oder", "") {
            @Override
            public Filter newInstance(List<Filter> filters) {
                return new OrFilter(filters);
            }
        });
        filters.put("nicht", new CombineFilterFactoryBase("nicht", "") {
            @Override
            public Filter newInstance(List<Filter> filters) {
                return new NotFilter(filters);
            }
        });
        return filters;
    }
}
