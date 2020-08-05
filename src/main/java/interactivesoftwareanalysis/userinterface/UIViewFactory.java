package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.data.DataType;

/**
 * A factory for instantiating a ui view
 */
public interface UIViewFactory {

    /**
     * Retrieve the data type that is supported by views instantiated by this factory
     * @return the supported data type
     */
    Class<? extends DataType> getSupportedDataType();

    /**
     * Create a new instance of a ui view
     * @param interactiveSubmodule the interactive submodule to create the view for
     * @return a new view instance
     */
    UIView newInstance(InteractiveSubmodule interactiveSubmodule);

    /**
     * Retrieve the views name for displaying in selection menus
     * @return the views name
     */
    String getName();
}
