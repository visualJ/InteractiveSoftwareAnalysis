package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.filter.DecideFilterFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for modules.
 */
@RequiredArgsConstructor public abstract class ModuleBase implements Module {

    protected final ModuleContext moduleContext;

    @Getter private final String name;
    @Getter private final String description;
    @Getter private List<ImportSubmodule> importSubmodules = new ArrayList<>();
    @Getter private List<ExportSubmodule> exportSubmodules = new ArrayList<>();
    @Getter private List<InteractiveSubmodule> interactiveSubmodules = new ArrayList<>();
    @Getter private List<DecideFilterFactory> filterFactories = new ArrayList<>();

}
