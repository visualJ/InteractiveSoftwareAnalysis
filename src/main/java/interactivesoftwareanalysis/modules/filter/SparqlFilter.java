package interactivesoftwareanalysis.modules.filter;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;

/**
 * A filter that uses a SPARQL ASK query to match resource items
 */
public class SparqlFilter extends DecideFilterBase {

    private final ModuleContext moduleContext;

    public SparqlFilter(String pattern, ModuleContext moduleContext) {
        super(pattern);
        this.moduleContext = moduleContext;
    }

    @Override public boolean filter(DataItem dataItem) {
        if (dataItem.getResource() == null) {
            return false;
        }
        String query = pattern.replace("?uri", "<" + dataItem.getResource().getUri() + ">");
        return moduleContext.getModel().executeAskQuery(query);
    }
}
