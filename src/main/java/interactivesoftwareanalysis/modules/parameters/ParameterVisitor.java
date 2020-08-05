package interactivesoftwareanalysis.modules.parameters;

/**
 * A visitor that visits parameters to create a UI or set their values
 */
public interface ParameterVisitor {
    void visitStringParameter(StringParameter stringParameter);

    void visitStringParameterWithChoices(StringParameter stringParameter);

    void visitStringParameterWithOnlyChoices(StringParameter stringParameter);

    void visitFileParameter(FileParameter fileParameter);
}
