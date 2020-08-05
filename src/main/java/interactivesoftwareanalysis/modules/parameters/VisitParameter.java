package interactivesoftwareanalysis.modules.parameters;

import interactivesoftwareanalysis.modules.actions.ModuleAction;
import interactivesoftwareanalysis.modules.actions.ModuleActionBase;
import interactivesoftwareanalysis.modules.parameters.Parameter;
import interactivesoftwareanalysis.modules.parameters.ParameterVisitor;

import java.lang.annotation.*;

/**
 * Declares the annotated field as a visitable parameter.
 * Fields (even private ones) of type {@link Parameter} that are annotated with this annotation are automatically
 * visited by a {@link ParameterVisitor} by the {@link ModuleActionBase} or {@link interactivesoftwareanalysis.modules.ExportSubmoduleBase}
 * when the visit method is called.
 * <br /><br />
 * Subclasses of {@link ModuleActionBase} or {@link interactivesoftwareanalysis.modules.ExportSubmoduleBase} can use this annotation to avoid having
 * to override the visit method defined in {@link ModuleAction} and visit each field manually.
 * <br /><br />
 * Keep in mind that this only considers fields directly declared in this class and not any field declared
 * elsewhere in the type hierarchy.
 * <br /><br />
 * If parameters should be visited in a different way, the visit method can be overridden.
 */
@Retention(RetentionPolicy.RUNTIME) @Documented @Target(ElementType.FIELD) public @interface VisitParameter {
}
