package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;

import java.util.List;

/**
 * An action that hides selected resources by tagging them with 'ausgeblendet'
 */
public class HideModuleAction extends ResourceSelectionModuleAction {

    public HideModuleAction(ModuleContext moduleContext) {
        super("Ausblenden", "Ressourcen als ausgeblendet markieren. Ausgeblendete Ressourcen können durch Herausfilterung des Tags 'ausgeblendet' versteckt werden.", moduleContext);
    }

    @Override public void execute(List<DataItem> input) {
        moduleContext.getModel().setBatchMode(true);
        input.stream()
                .map(DataItem::getResource)
                .filter(resource -> resource != null)
                .forEach(resource -> resource.addTag("ausgeblendet", "Wurde von einem Nutzer ausgeblendet."));
        moduleContext.getModel().setBatchMode(false);
    }
}
