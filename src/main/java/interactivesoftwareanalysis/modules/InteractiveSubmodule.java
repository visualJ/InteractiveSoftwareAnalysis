package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.actions.ModuleAction;
import interactivesoftwareanalysis.modules.data.DataType;
import interactivesoftwareanalysis.modules.data.DataTypeNotSupportedException;

import java.util.List;

/**
 * An interactive submodule can access and modify data in the model while a user interacts
 * with it by viewing output data and activating commands. An interactive submodule bundles
 * knowledge over certain aspects of the model and displays information relevant to that domain.
 */
public interface InteractiveSubmodule extends Describable {
    /**
     * Lists DataTypes that this interactive submodule can produce
     * @return a List of supported DataTypes
     */
    List<Class<? extends DataType>> getSupportedDataTypes();

    /**
     * Retrieves data from this interactive submodule.
     * @param dataType the requested data type class
     * @param <T> the data type type parameter
     * @return the requested data type with data this interactive submodule produces
     * @throws DataTypeNotSupportedException when the requested data type can not be produced by this interactive submodule
     */
    <T> T getData(Class<T> dataType) throws DataTypeNotSupportedException;

    /**
     * Lists module actions provided by this interactive submodule
     * @return a list of provided module actions
     */
    List<ModuleAction> getModuleActions();
}
