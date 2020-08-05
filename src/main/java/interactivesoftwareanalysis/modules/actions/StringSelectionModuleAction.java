package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;

import java.util.List;

/**
 * A base class for module actions that need an item selection. Items can contain resources.
 */
public abstract class StringSelectionModuleAction extends ModuleActionBase {
    public StringSelectionModuleAction(String name, String description, ModuleContext moduleContext) {
        super(name, description, moduleContext);
    }

    /**
     * Execute the action on selected items. Items can contain resources, but they don't have to.
     * @param input a list of selected data items
     */
    public abstract void execute(List<DataItem> input);

    @Override public void visit(ModuleActionVisitor moduleActionVisitor) {
        moduleActionVisitor.visitStringSelectionModuleAction(this);
    }
}
