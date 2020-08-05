package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.modules.parameters.VisitParameter;
import interactivesoftwareanalysis.modules.parameters.Parameter;
import interactivesoftwareanalysis.modules.parameters.ParameterVisitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * A base class for export submodules.
 */
@RequiredArgsConstructor public abstract class ExportSubmoduleBase implements ExportSubmodule {
    @Getter protected final String name;
    @Getter protected final String description;
    @Getter protected final ModuleContext moduleContext;

    /**
     * Visit all {@link Parameter} fields annotated with @{@link VisitParameter}.
     * @param parameterVisitor the visitor to visit parameters with
     */
    @Override @SneakyThrows(IllegalAccessException.class) public void visit(ParameterVisitor parameterVisitor) {
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (Parameter.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(VisitParameter.class)) {
                ((Parameter) field.get(this)).visit(parameterVisitor);
            }
        }
    }
}
