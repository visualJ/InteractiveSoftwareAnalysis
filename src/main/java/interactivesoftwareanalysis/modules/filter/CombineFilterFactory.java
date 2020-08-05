package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.Describable;

import java.util.List;

/**
 * A factory for creating combine filter instances
 */
public interface CombineFilterFactory extends Describable {

    /**
     * Create a new combine filter instance and initialize it
     * with the given filters
     * @param filters a list of filters the combine filter should combine
     * @return a new filter instance
     */
    Filter newInstance(List<Filter> filters);
}
