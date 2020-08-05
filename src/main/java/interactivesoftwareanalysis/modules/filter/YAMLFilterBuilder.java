package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.jena.sparql.sse.builders.BuilderExpr;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.parser.ParserException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A filter builder that creates filters from a YAML query string.
 * The query string is a YAML object that represents the hierarchical structure
 * of the filter.
 *
 * <p>Example:
 * <code>
 *     filterA: ["parameterA", filterB: "parameterB"]
 * </code>
 * creates a filterA with two decide filters: a default filter with a "parameterA" and the filterB with
 * a "parameterB".</p>
 */
public class YAMLFilterBuilder implements FilterBuilder {

    private static final String DEFAULT_FILTER = "text";
    private static final String DEFAULT_QUERY_WRAPPER = "und: [nicht: [tag: ausgeblendet], {0}]";

    private BooleanProperty useWrapper = new SimpleBooleanProperty(true);

    private class NoopFilter extends DecideFilterBase {

        public NoopFilter() {
            super("");
        }

        @Override
        public boolean filter(DataItem dataItem) {
            return true;
        }

    }

    @Override public Filter buildFilter(String query,
                                        Map<String, DecideFilterFactory> filterFactories,
                                        Map<String, CombineFilterFactory> combineFilterFactories) throws BuildFilterException {

        // wrap the query string, if needed, to provide additional default filtering
        if (getUseWrapper()){
            query = MessageFormat.format(DEFAULT_QUERY_WRAPPER, query);
        }

        Yaml yaml = new Yaml();
        Object node = null;
        try {
            node = yaml.load(query);
        } catch (YAMLException e) {
            throw new BuildFilterException("An error occured while parsing the query.", e);
        }

        // Create the default filter, when only a string is entered
        if (node instanceof String) {
            return filterFactories.get(DEFAULT_FILTER).newInstance((String) node);
        }

        // If it is a map, build a nested filter.
        if (node instanceof Map) {
            Map map = (Map) node;
            return getFilterForMap(map, filterFactories, combineFilterFactories);
        }

        // Otherwise, return a noop filter that does nothing.
        return new NoopFilter();
    }

    public boolean getUseWrapper() {
        return useWrapper.get();
    }

    @Override public BooleanProperty useWrapperProperty() {
        return useWrapper;
    }

    public void setUseWrapper(boolean useWrapper) {
        this.useWrapper.set(useWrapper);
    }

    private Filter getFilterForMap(Map map, Map<String, DecideFilterFactory> filterFactories, Map<String, CombineFilterFactory> combineFilterFactories) throws BuildFilterException {

        // A map object represents one filter instance.
        // The key is the filter name and the value is the parameter / parameters.
        // Get the filter name. It is not safe yet, that this is actually a string.
        Object filterNameObject = map.keySet().toArray()[0];

        // Check, if the filter name is a string. Otherwise, the input is incorrect.
        // and: [abc, tag:def]
        // -^-        -^-
        if (filterNameObject instanceof String) {

            // Cast to string and process the filter parameter(s)
            String filterName = (String) filterNameObject;
            Object parameter = map.get(filterName);

            // If the parameter is a string, the filter must be a DecideFilter.
            // Find the matching filter and instantiate it with the parameter as pattern.
            // and: [abc, tag:def]
            //                -^-
            if (parameter instanceof String) {
                if (filterFactories.containsKey(filterName)){
                    return filterFactories.get(filterName).newInstance((String) parameter);
                } else {
                    throw new BuildFilterException(MessageFormat.format("Filter ''{0}'' is unknown.", filterName));
                }
            }

            // If the parameter is a list, the filter must be a CombineFilter.
            // Process the parameters, find the matching filter and instantiate it with the parameters as sub filters.
            // and: [abc, tag:def]
            //      ------^-------
            if (parameter instanceof List){
                List parameters = (List) parameter;
                List<Filter> subFilters = new ArrayList<>();

                // Process the parameters.
                for (Object object : parameters){

                    // If the parameters is a string, instantiate the default filter
                    // with the string as pattern and remember it in subFilters.
                    // and: [abc, tag:def]
                    //       -^-
                    if (object instanceof String) {
                        if (filterFactories.containsKey(DEFAULT_FILTER)) {
                            subFilters.add(filterFactories.get(DEFAULT_FILTER).newInstance((String) object));
                        } else {
                            throw new BuildFilterException(MessageFormat.format("Default filter ''{0}'' is unknown.", DEFAULT_FILTER));
                        }
                    }

                    // If the parameter is a map, it represents itself another filter.
                    // Instantiate this filter recursively using this method and remember it in subFilters.
                    // and: [abc, tag:def]
                    //            ---^----
                    if (object instanceof Map) {
                        subFilters.add(getFilterForMap((Map) object, filterFactories, combineFilterFactories));
                    }
                }

                // Instantiate the correct combineFilter with the previously remembered subFilters.
                CombineFilterFactory factory = combineFilterFactories.get(filterName);
                if (factory != null) {
                    return factory.newInstance(subFilters);
                } else {
                    throw new BuildFilterException(MessageFormat.format("CombineFilter ''{0}'' is unknown.", filterName));
                }
            }
        }else{
            throw new BuildFilterException(MessageFormat.format("Any filter name must be a string. Got ''{0}'' instead.", filterNameObject.getClass()));
        }

        // If anything goes wrong, return a Noop filter that does nothing.
        return new NoopFilter();
    }
}
