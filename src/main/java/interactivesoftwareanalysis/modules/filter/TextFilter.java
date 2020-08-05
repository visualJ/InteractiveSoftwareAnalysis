package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that matches items that contain the pattern in any of their string values.
 */
public class TextFilter extends DecideFilterBase {

    public TextFilter(String pattern) {
        super(pattern);
    }

    @Override public boolean filter(DataItem dataItem) {
        if (dataItem.getString().toLowerCase().contains(pattern.toLowerCase())) {
            return true;
        }else{
            for (String s : dataItem.getStrings().values()) {
                if (s.toLowerCase().contains(pattern.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
