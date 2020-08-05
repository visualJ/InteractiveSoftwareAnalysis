package interactivesoftwareanalysis.modules.actions;

/**
 * A visitor for visiting different kinds of module actions for
 * creating UIs or executing them
 */
public interface ModuleActionVisitor {

    /**
     * Visit a module action that needs a resource selection
     * @param action the action to visit
     */
    void visitResourceSelectionModuleAction(ResourceSelectionModuleAction action);

    /**
     * Visit a module action that needs a selection
     * @param action the action to visit
     */
    void visitStringSelectionModuleAction(StringSelectionModuleAction action);

    /**
     * Visit a module action that does not need a selection
     * @param action the action to visit
     */
    void visitNoSelectionModuleAction(NoSelectionModuleAction action);
}
