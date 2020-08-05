package interactivesoftwareanalysis.modules.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A base class for combine filter factories
 */
@RequiredArgsConstructor public abstract class CombineFilterFactoryBase implements CombineFilterFactory {
    @Getter protected final String name;
    @Getter protected final String description;
}
