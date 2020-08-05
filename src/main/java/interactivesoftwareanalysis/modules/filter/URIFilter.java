package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that matches resources with the exact given URI
 */
public class URIFilter extends DecideFilterBase {

    public URIFilter(String pattern) {
        super(pattern);
    }

    @Override public boolean filter(DataItem dataItem) {
        if (dataItem.getResource() != null) {
            return dataItem.getResource().getUri().equals(pattern);
        } else {
            return false;
        }
    }
}
