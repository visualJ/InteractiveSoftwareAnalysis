package interactivesoftwareanalysis.modules.parameters;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A base class for parameters
 */
@RequiredArgsConstructor @EqualsAndHashCode public abstract class ParameterBase implements Parameter {

    @Getter protected final String name;
    @Getter protected final String description;

}
