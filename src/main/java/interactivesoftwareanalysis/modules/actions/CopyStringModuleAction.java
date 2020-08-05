package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An action that copies the selected items string values to the system clipboard; each in a separate line.
 */
public class CopyStringModuleAction extends StringSelectionModuleAction {

    public CopyStringModuleAction(ModuleContext moduleContext) {
        super("Text kopieren", "Kopiert den ausgewählten Text in die Zwischenablage", moduleContext);
    }

    @Override public void execute(List<DataItem> input) {

        // the clipboard can only be accessed in the application thread
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            String uris = input.stream().map(dataItem -> {
                if (dataItem.getStrings() != null && !dataItem.getStrings().isEmpty()){
                    // if multiple strings are available, join the together in a way,
                    // that is compatible with spreadsheet programs
                    return String.join("\t", dataItem.getStrings().values());
                }else{
                    return dataItem.getString();
                }
            }).collect(Collectors.joining(System.lineSeparator()));
            content.putString(uris);
            clipboard.setContent(content);
        });
    }
}
