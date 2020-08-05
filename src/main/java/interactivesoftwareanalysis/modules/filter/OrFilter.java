package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

import java.util.List;

/**
 * A combine filter that implements the logical OR for filters
 */
public class OrFilter extends CombineFilterBase {
    public OrFilter(List<Filter> filters) {
        super(filters);
    }

    @Override
    public boolean filter(DataItem dataItem) {
        return filters.stream().map(filter -> filter.filter(dataItem)).anyMatch(Boolean::booleanValue);
    }
}
