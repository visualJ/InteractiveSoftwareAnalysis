package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.Describable;
import interactivesoftwareanalysis.modules.parameters.ParameterVisitor;

/**
 * An action that can be defined by an interactive sub module.
 * The visitor pattern is used for creating a ui or executing the action.
 */
public interface ModuleAction extends Describable {

    /**
     * Visit this actions parameters with a {@link ParameterVisitor}
     * @param parameterVisitor the visitor to visit the parameters with
     */
    void visit(ParameterVisitor parameterVisitor);

    /**
     * Visit this action with a {@link ModuleActionVisitor}
     * @param moduleActionVisitor the visitor to use
     */
    void visit(ModuleActionVisitor moduleActionVisitor);
}
