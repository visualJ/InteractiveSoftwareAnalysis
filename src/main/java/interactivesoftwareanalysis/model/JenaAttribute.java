package interactivesoftwareanalysis.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * An attribute implementation for the {@link JenaRDFModel}.
 */
@ToString
@RequiredArgsConstructor
public class JenaAttribute implements Attribute {
    @Getter private final Model model;
    @Getter private final String value;
    @Getter private final Resource resource;
}
