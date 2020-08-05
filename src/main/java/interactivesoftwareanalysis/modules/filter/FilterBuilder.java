package interactivesoftwareanalysis.modules.filter;

import javafx.beans.property.BooleanProperty;

import java.util.Map;

/**
 * Build a filter from an input string
 */
public interface FilterBuilder {

    /**
     * Build a filter from a query string.
     * @param query The query to turn into a filter. The syntax is implementation dependant.
     * @param filterFactories factories for finding and creating filters
     * @param combineFilterFactories factories for finding and creating combine filters
     * @return a filter
     * @throws BuildFilterException when a filter could not be build with the given query string
     */
    Filter buildFilter(String query, Map<String, DecideFilterFactory> filterFactories, Map<String, CombineFilterFactory> combineFilterFactories) throws BuildFilterException;

    /**
     * Filters can wrap the query string to add default filter behavior.
     * This property determines whether the wrapper is used.
     * Implementations can decide how to set or define the wrapper.
     * @return a bindable property that is true, when the wrapper is used.
     */
    BooleanProperty useWrapperProperty();
}
