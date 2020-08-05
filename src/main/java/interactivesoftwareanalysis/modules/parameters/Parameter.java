package interactivesoftwareanalysis.modules.parameters;

import interactivesoftwareanalysis.modules.Describable;

/**
 * A parameter that can be visited to create a UI or set its value
 */
public interface Parameter extends Describable {

    /**
     * Visit the parameter with a {@link ParameterVisitor}
     * @param parameterVisitor the visitor to use
     */
    void visit(ParameterVisitor parameterVisitor);
}
