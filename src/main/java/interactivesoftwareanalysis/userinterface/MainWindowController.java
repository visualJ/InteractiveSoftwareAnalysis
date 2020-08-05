package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.model.Model;
import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.ModuleManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

/**
 * A controller for the main window.
 * The corresponding FXML file is layouts/main_window.fxml
 */
public class MainWindowController implements Initializable {

    @FXML private MenuButton perspectiveMenu;
    @FXML private TextField perspectiveName;
    @FXML private Pane windowPane;
    @FXML private MenuButton modulesMenu;
    @FXML private MenuButton modelMenu;
    @FXML private Menu exportMenu;
    @FXML private Pane modulePane;
    @FXML private Button saveButton;
    @FXML private Button loadButton;

    @Inject private UIManager uiManager;
    @Inject private ModuleManager moduleManager;
    @Inject private Model model;

    private class SwitchAnimation{

        private FadeTransition animationIn;
        private FadeTransition animationOut;
        SwitchAnimation(){
            Duration duration = Duration.millis(100);
            animationIn = new FadeTransition(duration);
            animationIn.setFromValue(0);
            animationIn.setToValue(1);
            animationOut = new FadeTransition(duration);
            animationOut.setFromValue(1);
            animationOut.setToValue(0);
        }

        void play(Node node, javafx.event.EventHandler<javafx.event.ActionEvent> onOutFinished){
            animationIn.setNode(node);
            animationOut.setNode(node);
            animationOut.setOnFinished(event -> {
                onOutFinished.handle(event);
                animationIn.playFromStart();
            });
            animationOut.playFromStart();
        }

    }
    private SwitchAnimation switchAnimation = new SwitchAnimation();

    @Override public void initialize(URL url, ResourceBundle resourceBundle) {

        // register listeners for new / removed perspectives
        uiManager.perspectivesProperty().addListener((ListChangeListener<? super UIPerspective>) c -> {
            while (c.next()) {
                c.getAddedSubList().stream()
                        .peek(this::buildUIPerspektiveMenu)
                        .forEach(this::registerUIPerspectiveListeners);
                c.getRemoved().forEach(this::removeUIPerspektiveMenu);
            }
        });

        // Build the module selection menu
        buildModulesMenu();

        // Build the model menu
        buildExportMenu();

        // Register listeners to show the new perspective, when it is switched
        registerPerspectiveSwitchListeners();

    }

    /**
     * Build the export menu by creating a menu item for each export module
     */
    private void buildExportMenu() {
        moduleManager.getExportSubmodules().stream().forEach(exportSubmodule -> {
            Label label = new Label(exportSubmodule.getName());
            Tooltip tooltip = new Tooltip(exportSubmodule.getDescription());
            label.setTooltip(tooltip);
            CustomMenuItem menuItem = new CustomMenuItem(label);
            menuItem.setOnAction(event -> {
                BuildUIParameterVisitor parameterDialog = new BuildUIParameterVisitor(exportSubmodule.getName(), exportSubmodule.getDescription(), windowPane.getScene().getWindow());
                exportSubmodule.visit(parameterDialog);
                boolean confirmed = parameterDialog.show();
                if (confirmed) {
                    exportSubmodule.export();
                }
            });
            exportMenu.getItems().add(menuItem);
        });
        if (moduleManager.getExportSubmodules().size() <= 0) {
            exportMenu.setDisable(true);
        }
    }

    /**
     * Register listeners for perspective switches to switch the views and
     * show animations.
     */
    private void registerPerspectiveSwitchListeners() {
        uiManager.currentPerspectiveProperty().addListener((observable, oldValue, newValue) -> {
            switchAnimation.play(modulePane, event -> {
                modulePane.getChildren().clear();
                for (UIView uiView : newValue.getUiViews()) {
                    modulePane.getChildren().add(uiView.getNode());
                }
                if(oldValue != null) {
                    perspectiveName.textProperty().unbindBidirectional(oldValue.nameProperty());
                }
                perspectiveName.textProperty().bindBidirectional(newValue.nameProperty());
                if (newValue.isPristine()){
                    newValue.setPristine(false);
                    perspectiveName.requestFocus();
                    perspectiveName.selectAll();
                }
            });
        });
    }

    /**
     * Build the menu for adding new modules. The menu lists all modules with interactive submodules as
     * sub menus. Those contain the modules interactive submodules as menu items.
     */
    private void buildModulesMenu() {
        StringToColorConverter converter = new HashStringToColorConverter();
        moduleManager.getModules().stream().filter(module -> !module.getInteractiveSubmodules().isEmpty()).forEach(module -> {
            Menu menu = new Menu(module.getName());
            for (InteractiveSubmodule submodule : module.getInteractiveSubmodules()) {
                Label label = new Label(submodule.getName());
                label.setTooltip(new Tooltip(submodule.getDescription()));
                label.setGraphic(new Circle(5, converter.getColor(submodule.getName())));
                CustomMenuItem menuItem = new CustomMenuItem(label);
                menuItem.setHideOnClick(false);
                menuItem.setOnAction(event -> {
                    UIPerspective currentPerspective = uiManager.getCurrentPerspective();
                    currentPerspective.addUIView(uiManager.newView(submodule));
                });
                menu.getItems().add(menuItem);
            }
            modulesMenu.getItems().add(menu);
        });
    }

    /**
     * Create and add a menu item for the ui perspective and create the necessary bindings.
     * @param uiPerspective The ui perspective to create the menu item for
     */
    private void buildUIPerspektiveMenu(final UIPerspective uiPerspective) {
        UIPerspectiveMenuItem menuItem = new UIPerspectiveMenuItem(uiPerspective, uiManager::closePerspective);

        // bind properties to the perspective object so they update automatically
//        menuItem.nameProperty().bind(Bindings.format("%s", uiPerspective.nameProperty()));
//        menuItem.viewCountProperty().bind(Bindings.format("%s", uiPerspective.uiViewsProperty().sizeProperty()));
        menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> uiPerspective == uiManager.getCurrentPerspective(),
                uiManager.currentPerspectiveProperty()));

        menuItem.setOnAction(e -> uiManager.setCurrentPerspective(uiPerspective));
        perspectiveMenu.getItems().add(menuItem);

        // set the perspective object as user data, so it can be removed later by the user (via removeUIPerspektiveMenu)
        menuItem.setUserData(uiPerspective);
    }

    /**
     * Remove the menu item of a ui perspective form the perspectives menu
     * @param uiPerspective the ui perspective to remove
     */
    private void removeUIPerspektiveMenu(final UIPerspective uiPerspective) {
        Platform.runLater(() -> perspectiveMenu.getItems().removeIf(menuItem -> menuItem.getUserData() == uiPerspective));
    }

    /**
     * Create and bind listeners to apply changes in a ui perspective to the actual ui
     * @param uiPerspective The ui perspective to add the liseners to
     */
    private void registerUIPerspectiveListeners(final UIPerspective uiPerspective) {
        uiPerspective.uiViewsProperty().addListener((ListChangeListener<? super UIView>) c -> {
            if (uiPerspective == uiManager.getCurrentPerspective()) {
                while (c.next()) {
                    for (UIView uiView : c.getAddedSubList()) {
                        modulePane.getChildren().add(uiPerspective.getUiViews().indexOf(uiView),uiView.getNode());
                    }
                    for (UIView uiView : c.getRemoved()) {
                        modulePane.getChildren().remove(uiView.getNode());
                    }
                }
            }
        });
    }

    /**
     * Create and open a new perspective
     */
    @FXML private void newPerspective(){
        uiManager.newPerspective();
    }

    /**
     * Close the current perspective and switch to another one
     */
    @FXML private void closeCurrentPerspective() {
        uiManager.closePerspective(uiManager.getCurrentPerspective());
    }

    /**
     * Show a file chooser to select a model file and load it
     */
    @FXML private void loadModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Modell laden");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML/RDF Modell", "*.xml"));
        File file = fileChooser.showOpenDialog(windowPane.getScene().getWindow());
        if (file != null) {
            try {
                InputStream stream = Files.newInputStream(file.toPath());
                model.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Show a file chooser to select the destination model file and save the model to it
     */
    @FXML private void saveModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Modell speichern");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML/RDF Modell", "*.xml"));
        File file = fileChooser.showSaveDialog(windowPane.getScene().getWindow());
        if (file != null) {
            try {
                OutputStream stream = Files.newOutputStream(file.toPath());
                model.save(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
