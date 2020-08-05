package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;

/**
 * A base class for module actions that do not need a selection
 */
public abstract class NoSelectionModuleAction extends ModuleActionBase {
    public NoSelectionModuleAction(String name, String description, ModuleContext moduleContext) {
        super(name, description, moduleContext);
    }

    /**
     * Execute the action
     */
    public abstract void execute();

    @Override public void visit(ModuleActionVisitor moduleActionVisitor) {
        moduleActionVisitor.visitNoSelectionModuleAction(this);
    }
}
