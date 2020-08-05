package interactivesoftwareanalysis.modules;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

/**
 * A progress that can be set by long running tasks.
 */
@RequiredArgsConstructor public class Progress {

    private final Consumer<Double> onProgressChanged;
    private final StringProperty message = new SimpleStringProperty();

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        Platform.runLater(() -> this.message.set(message));
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setProgress(double progress){
        onProgressChanged.accept(Double.min(1, Double.max(0, progress)));
    }
}
