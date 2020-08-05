package interactivesoftwareanalysis.userinterface;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import interactivesoftwareanalysis.model.events.ModelChangedEvent;
import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.modules.data.DataType;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the user interface.
 * The main window is created here and perspactives are managed.
 * Also, new views for interactive submodules are instantiated here.
 * Available view types can be registered here.
 */
public class UIManager {

    private ListProperty<UIPerspective> perspectives;
    private List<UIViewFactory> uiViewFactories;

    private ObjectProperty<UIPerspective> currentPerspective;
    private FXMLLoader fxmlLoader;
    private Scene scene;

    @Inject public UIManager(FXMLLoader fxmlLoader, ModuleManager moduleManager, EventBus eventBus) {
        this.fxmlLoader = fxmlLoader;
        eventBus.register(this);
        perspectives = new SimpleListProperty<>(FXCollections.observableArrayList());
        currentPerspective = new SimpleObjectProperty<>();

        // set the ui view factories. New view types must be registered here in order to show up as an option in the menu.
        uiViewFactories = Arrays.asList(
                UIListView.getFactory(this, moduleManager),
                UITreeView.getFactory(this, moduleManager),
                UITableView.getFactory(this, moduleManager)
        );

        // update the views, when the perspective is switched
        currentPerspective.addListener(observable -> getCurrentPerspective().getUiViews().forEach(uiView -> new Thread(uiView::update).start()));

        // load the icon font
        Font.loadFont(getClass().getResourceAsStream("/fonts/fontawesome-webfont.ttf"), 12);

        // INITIAL PERSPECTIVE CONFIURATION //

        Platform.runLater(() -> {
            UIPerspective perspective1 = new UIPerspective();
            perspectives.add(perspective1);

            List<InteractiveSubmodule> submodules = moduleManager.getInteractiveSubmodules();

            perspective1.setName("Übersicht");
            perspective1.setPristine(false);
            perspective1.addUIView(newView(submodules.get(2)));
            perspective1.addUIView(newView(submodules.get(0)));
            perspective1.addUIView(newView(submodules.get(4)));

            setCurrentPerspective(perspectives.get(0));
        });
    }

    /**
     * Initialies the ui manager and creates the main ui window.
     * @param stage the stage to show the ui in
     * @throws UIInitializeException when the ui manager could not be initialized
     */
    public void initialize(Stage stage) throws UIInitializeException {
        String mainWindowFXML = "/layouts/main_window.fxml";
        URL fxml = getClass().getResource(mainWindowFXML);
        if (fxml == null){
            throw new UIInitializeException("Could not initialize UIManager, because " + mainWindowFXML + " was not found");
        }
        fxmlLoader.setLocation(fxml);
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw  new UIInitializeException("Could not initialize UIManager", e);
        }
        stage.setTitle("Moddle");
        scene = new Scene(root, 1050, 750);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon16.png"), 16, 16, true, false));
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates and add a new perspective and sets it as the current one.
     * @return the new perspective
     */
    public UIPerspective newPerspective(){
        UIPerspective uiPerspective = new UIPerspective("Neue Perspektive");
        perspectives.add(uiPerspective);
        setCurrentPerspective(uiPerspective);
        return uiPerspective;
    }

    /**
     * Remove a perspective, if it's not the last one available
     * @param uiPerspective the perspective to remove
     * @return true, iff the perspective has been removed
     */
    public boolean closePerspective(UIPerspective uiPerspective){
        if (perspectives.size() <= 1) {
            return false;
        }
        perspectives.remove(uiPerspective);
        if (uiPerspective == getCurrentPerspective()) {
            setCurrentPerspective(perspectives.get(0));
        }
        return true;
    }

    /**
     * Create a new yiew for an interactive submodule with the modules default data type
     * @param interactiveSubmodule the interactive submodule to create the view for
     * @return a new view instance with the given submodule
     * @throws NoViewSupportsDataTypeException when interactive submodules defualt data type is not supported by any views
     */
    public UIView newView(InteractiveSubmodule interactiveSubmodule) throws NoViewSupportsDataTypeException {
        List<UIViewFactory> factories = getPreferredViews(interactiveSubmodule);
        if (!factories.isEmpty()){
            return factories.get(0).newInstance(interactiveSubmodule);
        }else{
            throw new NoViewSupportsDataTypeException("There are no views that support the data type of" + interactiveSubmodule);
        }
    }

    /**
     * Retrieves a list of views that support the default data type of an interactive submodule
     * @param interactiveSubmodule the interactive submodule to search views for
     * @return a list with views that support the interactive submodules default data type or an empty list
     */
    public List<UIViewFactory> getPreferredViews(InteractiveSubmodule interactiveSubmodule){
        Class<? extends DataType> dataType = interactiveSubmodule.getSupportedDataTypes().get(0);
        return uiViewFactories.stream().filter(uiViewFactory -> uiViewFactory.getSupportedDataType() == dataType).collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all views that support any data types supported by an interactive submodule
     * @param interactiveSubmodule the before mentioned interactive submodule
     * @return a list of views that can be used with this interactive submodule or an empty list
     */
    public List<UIViewFactory> getSupportedViews(InteractiveSubmodule interactiveSubmodule){
        List<Class<? extends DataType>> dataTypes = interactiveSubmodule.getSupportedDataTypes();
        return uiViewFactories.stream().filter(uiViewFactory -> dataTypes.contains(uiViewFactory.getSupportedDataType())).collect(Collectors.toList());
    }

    /**
     * Retrieve a list of perspectives
     * @return an unmodifiable list of perspectives
     */
    public List<UIPerspective> getPerspectives() {
        return Collections.unmodifiableList(perspectives.get());
    }

    /**
     * Retrieve the currently active perspective
     * @return the active perspective
     */
    public UIPerspective getCurrentPerspective() {
        return currentPerspective.get();
    }


    /**
     * Retrieve the property, which contains the currently active perspective
     * @return the current perspective property
     */
    public ObjectProperty<UIPerspective> currentPerspectiveProperty() {
        return currentPerspective;
    }

    /**
     * Retrieve the property with all perspectives
     * @return the perspectives property
     */
    public ListProperty<UIPerspective> perspectivesProperty() {
        return perspectives;
    }

    public void setCurrentPerspective(UIPerspective currentPerspective) {
        this.currentPerspective.set(currentPerspective);
    }

    /**
     * Display an information missing dialog.
     * @param message the message about what information is missing.
     */
    public void showInformationMissingDialog(String message){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getDialogPane().getStylesheets().add("styles/style.css");
            alert.setTitle("Es werden noch Informationen benötigt");
            alert.setHeaderText("");
            alert.setContentText(message);
            alert.initOwner(scene.getWindow());
            alert.show();
        });
    }

    /**
     * Display an error dialog.
     * @param message the message to display
     */
    public void showErrorDialog(String message){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add("styles/style.css");
            alert.setTitle("Fehler");
            alert.setHeaderText("");
            alert.setContentText(message);
            alert.initOwner(scene.getWindow());
            alert.show();
        });
    }

    /**
     * Update views in the current perspective on a model change
     * @param event the model changed event
     */
    @Subscribe private void onModelChanged(ModelChangedEvent event){
        getCurrentPerspective().getUiViews().forEach(uiView -> new Thread(uiView::update).start());
    }
}
