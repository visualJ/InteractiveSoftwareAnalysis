package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.ImportSubmodule;
import interactivesoftwareanalysis.modules.Module;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.modules.Progress;
import interactivesoftwareanalysis.modules.data.DataSourceCreateException;
import interactivesoftwareanalysis.modules.data.DataSourceManager;
import interactivesoftwareanalysis.modules.data.DataSourceProvider;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A controller for the import screen.
 * The corresponding FXML file is layouts/import_screen.fxml
 */
public class ImportScreenController implements Initializable {

    @Inject ModuleManager moduleManager;
    @Inject DataSourceManager dataSourceManager;
    @Inject UIManager uiManager;

    @FXML private Pane backgroundPane;
    @FXML private VBox dataSourcePane;
    @FXML private ListView<ImportSubmoduleSelection> importModuleList;
    @FXML private Pane importPane;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ChoiceBox<DataSourceProvider> dataSourceSelect;
    @FXML private Button importButton;
    @FXML private Button closeButton;
    @FXML private Label messageLabel;

    private List<ImportSubmoduleSelection> importSubmoduleSelections;

    private static class CloseAnimation {
        private ParallelTransition animation;
        CloseAnimation() {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(200));
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200));
            scaleTransition.setFromX(1);
            scaleTransition.setFromY(1);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.setInterpolator(Interpolator.EASE_IN);
            animation = new ParallelTransition(fadeTransition, scaleTransition);
        }

        void play(Node node, javafx.event.EventHandler<javafx.event.ActionEvent> onFinished) {
            animation.setOnFinished(onFinished);
            animation.setNode(node);
            animation.playFromStart();
        }
    }

    private CloseAnimation closeAnimation = new CloseAnimation();

    @RequiredArgsConstructor private class ImportSubmoduleSelection {
        @Getter private final ImportSubmodule importSubmodule;
        private BooleanProperty selected = new SimpleBooleanProperty(true);
        private BooleanProperty executed = new SimpleBooleanProperty(false);
        private BooleanProperty failed = new SimpleBooleanProperty(false);

        public boolean getExecuted() {
            return executed.get();
        }

        public BooleanProperty executedProperty() {
            return executed;
        }

        public void setExecuted(boolean executed) {
            this.executed.set(executed);
        }

        public boolean getFailed() {
            return failed.get();
        }

        public BooleanProperty failedProperty() {
            return failed;
        }

        public void setFailed(boolean failed) {
            this.failed.set(failed);
        }

        public boolean getSelected() {
            return selected.get();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        @Override public String toString() {
            return importSubmodule.getName();
        }
    }

    @Override public void initialize(URL url, ResourceBundle resourceBundle) {

        // prepare the data source selection fields
        dataSourceSelect.setItems(dataSourceManager.getDataSourceProviders());
        dataSourceSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldProvider, newProvider) -> {
            dataSourcePane.getChildren().clear();
            Node newProviderUI = newProvider.getUI();
            if (newProviderUI != null) {
                dataSourcePane.getChildren().add(newProviderUI);
            }
        });
        dataSourceSelect.getSelectionModel().selectFirst();

        // set the cell factory for the import module selection list
        importModuleList.setCellFactory(param -> {
            CheckBoxListCell<ImportSubmoduleSelection> listCell = new CheckBoxListCell<>(ImportSubmoduleSelection::selectedProperty);
            listCell.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    listCell.setTooltip(new Tooltip(newValue.getImportSubmodule().getDescription()));
                    newValue.executedProperty().addListener(observable1 -> listCell.getStyleClass().add("import-executed"));
                    newValue.failedProperty().addListener(observable1 -> listCell.getStyleClass().add("import-failed"));
                }
            });
            return listCell;
        });

        // prepare the import module selection list
        importSubmoduleSelections = new ArrayList<>();
        moduleManager.getModules().stream()
                .map(Module::getImportSubmodules)
                .flatMap(List::stream)
                .forEach(importSubmodule -> importSubmoduleSelections.add(new ImportSubmoduleSelection(importSubmodule)));
        importModuleList.getItems().addAll(importSubmoduleSelections);
    }

    /**
     * Begin importing data
     * @param actionEvent the gui action event
     */
    @FXML private void beginImport(ActionEvent actionEvent) {
        new Thread(() -> {

            // check if any import modules are selected
            long selectedCount = importSubmoduleSelections.stream().filter(ImportSubmoduleSelection::getSelected).count();
            if (selectedCount <= 0) {
                uiManager.showInformationMissingDialog("Es sind keine Importmodule ausgewählt.");
                return;
            }

            // show the progress indicator
            Platform.runLater(() -> progressIndicator.setVisible(true));
            Platform.runLater(() -> importButton.setDisable(true));

            try {
                // Create the data source
                dataSourceManager.setDataSource(dataSourceSelect.getSelectionModel().getSelectedItem().getDataSource());
            } catch (DataSourceCreateException e) {
                uiManager.showErrorDialog(e.getMessage());
                reset();
                return;
            }

            // Prepare progress tracking
            AtomicInteger executedCount = new AtomicInteger();
            Progress progress = new Progress(newProgress ->
                    Platform.runLater(() ->
                            progressIndicator.setProgress(((executedCount.get() + newProgress) / selectedCount))));
            Platform.runLater(() -> messageLabel.textProperty().bind(progress.messageProperty()));

            // Execute the import modules
            importSubmoduleSelections.stream()
                .filter(ImportSubmoduleSelection::getSelected)
                .peek(selection -> {
                    boolean success = selection.getImportSubmodule().doImport(progress);
                    if (success) {
                        selection.setExecuted(true);
                    } else {
                        selection.setFailed(true);
                    }
                }).forEach(selection -> Platform.runLater(() -> {
                    executedCount.incrementAndGet();
                    progressIndicator.setProgress(executedCount.get() / (double) selectedCount);
                }));

            // report any failed imports
            reportFailedImports();

            // close the import dialog
            reset();
            closeAnimation.play(backgroundPane, event -> backgroundPane.setVisible(false));

        }, "Import Thread").start();
    }

    /**
     * Close the import screen
     */
    @FXML private void close() {
        reset();
        closeAnimation.play(backgroundPane, event -> backgroundPane.setVisible(false));
    }

    /**
     * Reset the screen to default state
     */
    private void reset() {
        Platform.runLater(() -> {
            Platform.runLater(() -> importButton.setDisable(false));
            progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            progressIndicator.setVisible(false);
        });
    }

    /**
     * Display a message with a list of failed import modules
     */
    private void reportFailedImports() {
        List<String> failedImports = importSubmoduleSelections.stream()
                .filter(ImportSubmoduleSelection::getFailed)
                .map(ImportSubmoduleSelection::getImportSubmodule)
                .map(ImportSubmodule::getName)
                .collect(Collectors.toList());
        int failedCount = failedImports.size();
        if (failedCount > 0) {
            uiManager.showErrorDialog("Anzahl fehlgeschlagener Imports: " + failedCount + "\n\n" + String.join("\n", failedImports));
        }
    }
}
