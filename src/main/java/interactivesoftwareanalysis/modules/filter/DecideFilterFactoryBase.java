package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.ModuleContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A base class for decide filter factories
 */
@RequiredArgsConstructor public abstract class DecideFilterFactoryBase implements DecideFilterFactory {
    @Getter protected final String name;
    @Getter protected final String description;

    @Getter protected ModuleContext moduleContext;

    public DecideFilterFactoryBase(String name, String description, ModuleContext moduleContext) {
        this.name = name;
        this.description = description;
        this.moduleContext = moduleContext;
    }
}
