package interactivesoftwareanalysis.model;

import lombok.Data;

/**
 * A tag a {@link Resource} can be tagged with.
 */
@Data public class Tag {
    private final String name;
    private final String detail;
}
