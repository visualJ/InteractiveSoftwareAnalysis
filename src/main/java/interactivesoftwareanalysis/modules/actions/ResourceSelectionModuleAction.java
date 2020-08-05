package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;

import java.util.List;

/**
 * A base class for module actions that need a resource selection
 */
public abstract class ResourceSelectionModuleAction extends ModuleActionBase {
    public ResourceSelectionModuleAction(String name, String description, ModuleContext moduleContext) {
        super(name, description, moduleContext);
    }

    /**
     * Execute the action on a list of selected items.
     * All data items MUST contain a resource.
     * @param input as list of selected data items
     */
    public abstract void execute(List<DataItem> input);

    @Override public void visit(ModuleActionVisitor moduleActionVisitor) {
        moduleActionVisitor.visitResourceSelectionModuleAction(this);
    }
}
