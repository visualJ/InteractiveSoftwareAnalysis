package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.parameters.ParameterVisitor;

/**
 * A submodule that exports data
 */
public interface ExportSubmodule extends Describable {

    /**
     * Export data.
     * The implementation decides how and which data to export.
     */
    void export();

    /**
     * Visit the export submodule with a {@link ParameterVisitor},
     * so that parameters can be set by it.
     * @param parameterVisitor
     */
    void visit(ParameterVisitor parameterVisitor);
}
