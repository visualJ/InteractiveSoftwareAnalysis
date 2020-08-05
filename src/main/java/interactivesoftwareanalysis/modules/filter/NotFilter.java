package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

import java.util.List;
import java.util.Optional;

/**
 * A combine filter that implements a logical NOT to negate the first filter
 */
public class NotFilter extends CombineFilterBase {
    public NotFilter(List<Filter> filters) {
        super(filters);
    }

    @Override
    public boolean filter(DataItem dataItem) {
        Optional<Boolean> optional = filters.stream().map(filter -> filter.filter(dataItem)).findFirst();
        if (optional.isPresent()) {
            return !optional.get();
        }else{
            return true;
        }
    }
}
