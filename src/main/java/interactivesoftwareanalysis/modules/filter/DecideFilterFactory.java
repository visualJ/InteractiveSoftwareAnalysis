package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.Describable;

/**
 * A factory for creating decide filter instances
 */
public interface DecideFilterFactory extends Describable {

    /**
     * Create a new decide filter instance with the given pattern
     * @param pattern the pattern the new filter should use
     * @return a new filter instance
     */
    Filter newInstance(String pattern);
}
