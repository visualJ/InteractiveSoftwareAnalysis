package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.actions.*;
import interactivesoftwareanalysis.modules.data.DataType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A base class for interactive submodules
 */
public abstract class InteractiveSubmoduleBase implements InteractiveSubmodule {

    @NonNull protected final ModuleContext moduleContext;

    @NonNull @Getter private final List<Class<? extends DataType>> supportedDataTypes;
    @NonNull @Getter protected final String name;
    @NonNull @Getter protected final String description;
    @NonNull @Getter @Setter private List<ModuleAction> moduleActions;

    protected InteractiveSubmoduleBase(String name, String description, List<Class<? extends DataType>> supportedDataTypes, ModuleContext moduleContext) {
        this.supportedDataTypes = supportedDataTypes;
        this.name = name;
        this.description = description;
        this.moduleContext = moduleContext;

        // add default actions
        this.moduleActions = new ArrayList<>(Arrays.asList(
                new HideModuleAction(moduleContext),
                new TagModuleAction(moduleContext),
                new CopyStringModuleAction(moduleContext),
                new CopyURIModuleAction(moduleContext),
                new RemoveAllTagsModuleAction(moduleContext)
        ));
    }
}
