package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.filter.DecideFilterFactory;

import java.util.List;

/**
 * A module bundles submodules that have knowledge of a certain domain and provide
 * import, export and processing funktions for the model.
 */
public interface Module extends Describable {

    /**
     * Retrieve this modules import submodules.
     * @return a list with import submodules this module contains or an empty list.
     */
    List<ImportSubmodule> getImportSubmodules();

    /**
     * Retrieve this modules export submodules.
     * @return a list with export submodules this module contains or an empty list.
     */
    List<ExportSubmodule> getExportSubmodules();

    /**
     * Retrieve this modules interactive submodules.
     * @return a list with interactive submodules this module contains or an empty list.
     */
    List<InteractiveSubmodule> getInteractiveSubmodules();

    /**
     * Retrieve this modules filter factories
     * @return a list with filters submodules this module contains or an empty list.
     */
    List<DecideFilterFactory> getFilterFactories();

}
