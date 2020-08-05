package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

import java.util.List;

/**
 * A filter taht uses the logical AND to combine several filters results in a short circuiting way.
 */
public class AndFilter extends CombineFilterBase {
    public AndFilter(List<Filter> filters) {
        super(filters);
    }

    @Override
    public boolean filter(DataItem dataItem) {
        return !filters.stream()
                .map(filter -> filter.filter(dataItem))
                .anyMatch(result -> !result);
    }
}
