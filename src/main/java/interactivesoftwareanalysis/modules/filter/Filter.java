package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that decides, whether a data item matches, or not
 */
@FunctionalInterface public interface Filter {

    /**
     * Decide, whether a data item matches or not.
     * @param dataItem the data item to match
     * @return true, iff the filter matches the data item
     */
    boolean filter(DataItem dataItem);
}
