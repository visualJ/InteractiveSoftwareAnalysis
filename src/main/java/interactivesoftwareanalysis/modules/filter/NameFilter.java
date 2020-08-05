package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that filters items that have a name that contains the pattern
 */
public class NameFilter extends DecideFilterBase {

    public NameFilter(String pattern) {
        super(pattern);
    }

    @Override public boolean filter(DataItem dataItem) {
        return dataItem.getString().toLowerCase().contains(pattern.toLowerCase());
    }
}
