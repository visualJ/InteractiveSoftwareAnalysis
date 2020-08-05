package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.parameters.Parameter;
import interactivesoftwareanalysis.modules.parameters.ParameterVisitor;
import interactivesoftwareanalysis.modules.parameters.VisitParameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * A base class for module actions.
 */
@RequiredArgsConstructor public abstract class ModuleActionBase implements ModuleAction {

    @Getter protected final String name;
    @Getter protected final String description;

    protected final ModuleContext moduleContext;

    /**
     * Visit all fields of type {@link Parameter} annotated with @{@link VisitParameter}
     * @param parameterVisitor the visitor to visit the parameters with
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
