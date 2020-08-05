package interactivesoftwareanalysis.modules.filter;

import com.sun.org.apache.regexp.internal.RE;
import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that matches a regular expression against items names
 */
public class RegexFilter extends DecideFilterBase {

    public RegexFilter(String pattern) {
        super(pattern);
    }

    @Override public boolean filter(DataItem dataItem) {
        RE re = new RE(pattern);
        return re.match(dataItem.getString());
    }
}
