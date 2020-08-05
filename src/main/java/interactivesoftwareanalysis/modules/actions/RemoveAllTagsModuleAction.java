package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;
import interactivesoftwareanalysis.modules.parameters.StringParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A module action that removes all tags form selected resources
 */
public class RemoveAllTagsModuleAction extends ResourceSelectionModuleAction {

    public RemoveAllTagsModuleAction(ModuleContext moduleContext) {
        super("Alle Tags entfernen", "Entfernt alle Tags von Ressourcen", moduleContext);
    }

    @Override public void execute(List<DataItem> input) {

        // removing a lot of tags might take a while, so don't block the gui thread.
        new Thread(() -> {
            moduleContext.getModel().setBatchMode(true);

            // select all tag statements of this resource in the model
            // and remove them
            for (DataItem dataItem : input) {
                String resourceUri = dataItem.getResource().getUri();
                String propertyUri = "http://interactivesoftwareanalysis/tag";
                moduleContext.getModel().removeStatements(resourceUri, propertyUri);
            }
            moduleContext.getModel().setBatchMode(false);
        }).start();
    }
}
