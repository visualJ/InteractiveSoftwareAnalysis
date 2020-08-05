package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An action that copies selected resources URIs to the system clipboard
 */
public class CopyURIModuleAction extends ResourceSelectionModuleAction {

    public CopyURIModuleAction(ModuleContext moduleContext) {
        super("URI kopieren", "Kopiert den URI der ausgewählten Ressource in die Zwischenablage", moduleContext);
    }

    @Override public void execute(List<DataItem> input) {

        // the clipboard can only be accessed on the applicaiton thread
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            // put each uri on a separate line
            String uris = input.stream().map(dataItem -> dataItem.getResource().getUri()).collect(Collectors.joining(System.lineSeparator()));
            content.putString(uris);
            clipboard.setContent(content);
        });
    }
}
