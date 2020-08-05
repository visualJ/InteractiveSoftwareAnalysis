package interactivesoftwareanalysis.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A base class for import submodules
 */
@RequiredArgsConstructor public abstract class ImportSubmoduleBase implements ImportSubmodule {
    @Getter protected final String name;
    @Getter protected final String description;
    @Getter protected final ModuleContext moduleContext;
}
