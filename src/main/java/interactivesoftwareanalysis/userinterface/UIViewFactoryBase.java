package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.ModuleManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A base class for ui view factories
 */
@RequiredArgsConstructor public abstract class UIViewFactoryBase implements UIViewFactory {

    @Getter protected final String name;
    protected final UIManager uiManager;
    protected final ModuleManager moduleManager;

}
