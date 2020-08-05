package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that matches resources tagged with the exact tag name
 */
public class TagFilter extends DecideFilterBase {

    public TagFilter(String pattern) {
        super(pattern);
    }

    @Override public boolean filter(DataItem dataItem) {
        return pattern.isEmpty() || dataItem.getTags().stream().anyMatch(tag -> tag.getName().equals(pattern));
    }
}
