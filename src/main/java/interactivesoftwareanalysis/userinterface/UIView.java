package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.modules.actions.*;
import interactivesoftwareanalysis.modules.data.DataItem;
import interactivesoftwareanalysis.modules.data.DataType;
import interactivesoftwareanalysis.modules.filter.BuildFilterException;
import interactivesoftwareanalysis.modules.filter.Filter;
import interactivesoftwareanalysis.modules.filter.FilterBuilder;
import interactivesoftwareanalysis.modules.filter.YAMLFilterBuilder;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.*;
import org.controlsfx.control.textfield.CustomTextField;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by benedikt.ringlein on 25.08.2016.
 */
@ToString @EqualsAndHashCode(exclude = { "uiPerspective" }) public abstract class UIView implements Initializable {

    /** The base width of a view. The actual width can ba a multiple od this */
    private static final int BASE_WIDTH = 300;

    /** The base height of a view. The actual height can ba a multiple od this */
    private static final int BASE_HEIGHT = 300;

    /** The width of the space between views */
    private static final int SPACE_WIDTH = 10;

    /** The height of the space between views */
    private static final int SPACE_HEIGHT = 10;

    /** The duration of animations in ms */
    private static final int ANIMATION_TIME = 100;

    @Getter protected final InteractiveSubmodule interactiveSubmodule;
    protected final ModuleManager moduleManager;
    protected final UIManager uiManager;
    @Getter private final Class<? extends DataType> dataType;
    private final FilterBuilder filterBuilder;

    @FXML protected MenuButton actionMenu;
    @FXML protected BorderPane contentPane;
    @FXML protected Label moduleNameLabel;
    @FXML protected Pane resizeHorizontal;
    @FXML protected Pane resizeVertical;
    @FXML protected CustomTextField filterField;
    @FXML protected CheckMenuItem showHiddenCheck;
    @FXML protected Label filterErrorLabel;
    @FXML protected ProgressIndicator progressIndicator;
    @FXML protected Pane iconPane;
    protected ContextMenu contextMenu;
    protected Filter filter;

    @Getter @Setter @NonNull private UIPerspective uiPerspective;
    private ListProperty<DataItem> selection;
    private IntegerProperty widthFactor = new SimpleIntegerProperty(1);
    private IntegerProperty heightFactor = new SimpleIntegerProperty(2);
    private IntegerProperty selectedItemsCount = new SimpleIntegerProperty(0);
    private IntegerProperty selectedResourcesCount = new SimpleIntegerProperty(0);
    private Node content;
    private OpenAnimation openAnimation = new OpenAnimation();
    private CloseAnimation closeAnimation = new CloseAnimation();

    @SneakyThrows(IOException.class)
    UIView(InteractiveSubmodule interactiveSubmodule, Class<? extends DataType> dataType, UIManager uiManager, ModuleManager moduleManager) {
        this.dataType = dataType;
        this.uiManager = uiManager;
        this.interactiveSubmodule = interactiveSubmodule;
        this.moduleManager = moduleManager;
        this.contextMenu = new ContextMenu();
        this.filterBuilder = new YAMLFilterBuilder();
        this.selection = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.contextMenu.getItems().addAll(createActionMenuItems());

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/layouts/ui_view.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.load();
    }

    @Override @SneakyThrows(BuildFilterException.class)
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filterBuilder.useWrapperProperty().bind(showHiddenCheck.selectedProperty().not());
        showHiddenCheck.selectedProperty().addListener(observable -> filter());

        // bind panel size to size factor properties
        contentPane.prefWidthProperty()
                .bind(Bindings.createIntegerBinding(() -> widthFactorProperty().get() * BASE_WIDTH + SPACE_WIDTH * (widthFactorProperty().get() - 1), widthFactorProperty()));
        contentPane.maxWidthProperty().bind(contentPane.prefWidthProperty());
        contentPane.prefHeightProperty()
                .bind(Bindings.createIntegerBinding(() -> heightFactorProperty().get() * BASE_HEIGHT + SPACE_HEIGHT * (heightFactorProperty().get() - 1), heightFactorProperty()));
        contentPane.maxHeightProperty().bind(contentPane.prefHeightProperty());

        //bind selection properties
        selectedItemsCount.bind(selectionProperty().sizeProperty());
        selectedResourcesCount.bind(Bindings.createIntegerBinding(() -> (int) getSelection().stream().filter(dataItem -> dataItem.getResource() != null).count(), selectionProperty()));

        // Create drag gestures
        new DragHorizontalGesture(moduleNameLabel, this::swapLeft, this::swapRight);
        new DragHorizontalGesture(resizeHorizontal, this::decreaseWidth, this::increaseWidth);
        new DragVerticalGesture(resizeVertical, this::decreaseHeight, this::increaseHeight);

        // Show the connected interactive submodules name
        moduleNameLabel.setText(getInteractiveSubmodule().getName());

        // register listeners that trigger animations when the position in the layout changes
        registerLayoutAnimationListeners();

        // build the action menu
        buildActionMenu();

        // add the color accent
        StringToColorConverter converter = new HashStringToColorConverter();
//        moduleNameLabel.setGraphic(new Circle(5, converter.getColor(interactiveSubmodule.getName())));
        iconPane.getChildren().add(new Circle(4, converter.getColor(interactiveSubmodule.getName())));

        // set the standard filter
        this.filter = filterBuilder.buildFilter(filterField.getText(), moduleManager.getFilterFactories(), moduleManager.getCombineFilterFactories());

        // play the opening animation
        openAnimation.play(contentPane);
    }

    /**
     * Retreive the current selection
     * @return a list of selected data items
     */
    public ObservableList<DataItem> getSelection() {
        return selection.get();
    }

    /**
     * Retrieve the selection property
     * @return the selection property
     */
    public ListProperty<DataItem> selectionProperty() {
        return selection;
    }

    /**
     * Update the views data
     */
    public abstract void update();

    /**
     * Update the filtered data
     */
    public abstract void updateFilter();

    /**
     * Retrieve the content node
     * @return the node displayed as this views content
     */
    public Node getNode() {
        return contentPane;
    }

    /**
     * Set this views content
     * @param node a node to display as content
     */
    public void setContent(Node node) {
        content = node;
        contentPane.setCenter(content);
    }

    /**
     * Retrieve the width factor.
     * The width factor is multiplied with the BASE_WIDTH + SPACE_WIDTH ({@value BASE_WIDTH} + {@value SPACE_WIDTH})
     * @return the current width factor
     */
    public int getWidthFactor() {
        return widthFactor.get();
    }

    /**
     * Set the width factor
     * @param widthFactor the new factor to multiply with the BASE_WIDTH
     */
    public void setWidthFactor(int widthFactor) {
        this.widthFactor.set(widthFactor);
    }

    /**
     * Retrieve the property for the width factor
     * @return the width factor property
     */
    public IntegerProperty widthFactorProperty() {
        return widthFactor;
    }

    /**
     * Retrieve the height factor.
     * The width factor is multiplied with the BASE_HEIGHT + SPACE_HEIGHT ({@value BASE_HEIGHT} + {@value SPACE_HEIGHT})
     * @return the current wiheightdth factor
     */
    public int getHeightFactor() {
        return heightFactor.get();
    }

    /**
     * Set the height factor
     * @param heightFactor the new factor to multiplay with the BASE_HEIGHT
     */
    public void setHeightFactor(int heightFactor) {
        this.heightFactor.set(heightFactor);
    }

    /**
     * Retrieve the property for the width factor
     * @return the width factor property
     */
    public IntegerProperty heightFactorProperty() {
        return heightFactor;
    }

    /**
     * Shows or hides the progress indicator
     * @param value true to show and false to hide the progress indicator
     */
    protected void setProgressVisible(boolean value) {
        Platform.runLater(() -> progressIndicator.setVisible(value));
    }

    /**
     * Swap this views position with the one left to it
     */
    private void swapLeft() {
        uiPerspective.swapViewLeft(this);
    }

    /**
     * Swap this views position with the one right to it
     */
    private void swapRight() {
        uiPerspective.swapViewRight(this);
    }

    /**
     * Close this view
     */
    @FXML private void closeView() {
        closeAnimation.play(contentPane, event -> uiPerspective.removeUIView(this));
    }

    /**
     * Apply the filter to the data
     */
    @FXML private void filter() {
        try {
            this.filter = filterBuilder.buildFilter(filterField.getText(), moduleManager.getFilterFactories(), moduleManager.getCombineFilterFactories());
            filterErrorLabel.setVisible(false);
            filterErrorLabel.setManaged(false);
            updateFilter();
        } catch (BuildFilterException e) {
            filterErrorLabel.setVisible(true);
            filterErrorLabel.setManaged(true);
        }
    }

    /**
     * Show a message with available filter names and descriptions
     */
    @FXML private void showAvailableFilters() {
        String filters = moduleManager.getFilterFactories().values().stream()
                .map(factory -> MessageFormat.format("{0}: {1}", factory.getName(), factory.getDescription())).collect(Collectors.joining("\n"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, filters, ButtonType.OK);
        alert.getDialogPane().getStylesheets().add("styles/style.css");
        alert.setHeaderText("Diese Filter sind verfügbar und können jeweils mit einem Parameter genutzt werden.");
        alert.setTitle("Verfügbare Filter");
        alert.initOwner(content.getScene().getWindow());
        alert.show();
    }

    /**
     * Increase the width factor by one
     */
    private void increaseWidth() {
        setWidthFactor(getWidthFactor() + 1);
    }

    /**
     * Decrease the width factor by one
     */
    private void decreaseWidth() {
        setWidthFactor(Math.max(1, getWidthFactor() - 1));
    }

    /**
     * Increase the height factor by one
     */
    private void increaseHeight() {
        setHeightFactor(getHeightFactor() + 1);
    }

    /**
     * Decrease teh hight factor by one
     */
    private void decreaseHeight() {
        setHeightFactor(Math.max(1, getHeightFactor() - 1));
    }

    /**
     * Register listeners for animating layout changes
     */
    private void registerLayoutAnimationListeners() {
        contentPane.layoutXProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.intValue() == 0)
                return;
            contentPane.setTranslateX(oldValue.doubleValue() - newValue.doubleValue());
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(ANIMATION_TIME), contentPane);
            translateTransition.setFromX(oldValue.doubleValue() - newValue.doubleValue());
            translateTransition.setToX(0);
            translateTransition.playFromStart();
        });
        contentPane.layoutYProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.intValue() == 0)
                return;
            contentPane.setTranslateY(oldValue.doubleValue() - newValue.doubleValue());
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(ANIMATION_TIME), contentPane);
            translateTransition.setFromY(oldValue.doubleValue() - newValue.doubleValue());
            translateTransition.setToY(0);
            translateTransition.playFromStart();
        });
    }

    /**
     * Build the action menu by creating a menu item for each supported view and appending the action
     * menu items.
     */
    private void buildActionMenu() {
        List<UIViewFactory> supportedViews = uiManager.getSupportedViews(getInteractiveSubmodule());

        // create the view change menu
        for (UIViewFactory viewFactory : supportedViews) {
            MenuItem menuItem = new MenuItem("Als " + viewFactory.getName() + " anzeigen");
            menuItem.setOnAction(event -> {
                UIPerspective uiPerspective = getUiPerspective();
                UIView newView = viewFactory.newInstance(getInteractiveSubmodule());
                newView.setWidthFactor(getWidthFactor());
                newView.setHeightFactor(getHeightFactor());
                newView.filter = filter;
                newView.filterField.setText(filterField.getText());
                uiPerspective.swapView(this, newView);
            });
            actionMenu.getItems().add(menuItem);
        }
        actionMenu.getItems().add(new SeparatorMenuItem());

        // create the actual action menu part
        actionMenu.getItems().addAll(createActionMenuItems());
    }

    /**
     * Create the menu with module actions. This can be used in the aciton menu or in context menus
     * @return a list with action menu items
     */
    private ObservableList<MenuItem> createActionMenuItems(){
        ObservableList<MenuItem> items = FXCollections.observableArrayList();
        for (ModuleAction moduleAction : getInteractiveSubmodule().getModuleActions()) {
            MenuItem menuItem = new MenuItem("_"+moduleAction.getName());
            menuItem.setMnemonicParsing(true);

            // disable actions that can not be executed
            moduleAction.visit(new ModuleActionVisitor() {
                @Override public void visitResourceSelectionModuleAction(ResourceSelectionModuleAction action) {
                    menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> getSelection().stream()
                            .allMatch(dataItem -> dataItem.getResource() == null), selectionProperty()));
                    menuItem.textProperty().bind(Bindings.format("_%s (%d)", moduleAction.getName(), selectedResourcesCount));
                }

                @Override public void visitStringSelectionModuleAction(StringSelectionModuleAction action) {
                    menuItem.disableProperty().bind(selectionProperty().emptyProperty());
                    menuItem.textProperty().bind(Bindings.format("_%s (%d)", moduleAction.getName(), selectedItemsCount));
                }

                @Override public void visitNoSelectionModuleAction(NoSelectionModuleAction action) {
                    // those action are always possible, as they do not need a selection
                }
            });
            menuItem.setOnAction(event -> {
                BuildUIParameterVisitor parameterDialog = new BuildUIParameterVisitor(moduleAction.getName(), moduleAction.getDescription(), contentPane.getScene().getWindow());
                moduleAction.visit(parameterDialog);
                boolean confirmed = parameterDialog.show();
                if (confirmed) {
                    moduleAction.visit(new ModuleActionVisitor() {
                        @Override public void visitResourceSelectionModuleAction(ResourceSelectionModuleAction action) {
                            List<DataItem> selection = getSelection().stream()
                                    .filter(dataItem -> dataItem.getResource() != null)
                                    .collect(Collectors.toList());
                            if (selection.size() > 0) {
                                new Thread(() -> action.execute(selection)).start();
                            }
                        }

                        @Override public void visitStringSelectionModuleAction(StringSelectionModuleAction action) {
                            List<DataItem> selection = getSelection();
                            if (selection.size() > 0) {
                                new Thread(() -> action.execute(selection)).start();
                            }
                        }

                        @Override public void visitNoSelectionModuleAction(NoSelectionModuleAction action) {
                            new Thread(action::execute).start();
                        }
                    });
                }
            });
            items.add(menuItem);
        }
        return items;
    }

    private static class OpenAnimation {

        private ScaleTransition animation;

        OpenAnimation() {
            animation = new ScaleTransition(Duration.millis(ANIMATION_TIME));
            animation.setFromX(0.95);
            animation.setFromY(0.95);
            animation.setToX(1);
            animation.setToY(1);
            animation.setInterpolator(Interpolator.EASE_OUT);
        }

        void play(Node node) {
            animation.setNode(node);
            animation.playFromStart();
        }

    }

    private static class CloseAnimation {

        private ParallelTransition animation;

        CloseAnimation() {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(ANIMATION_TIME));
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(ANIMATION_TIME));
            scaleTransition.setFromX(1);
            scaleTransition.setFromY(1);
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
            scaleTransition.setInterpolator(Interpolator.EASE_IN);
            animation = new ParallelTransition(fadeTransition, scaleTransition);
        }

        void play(Node node, javafx.event.EventHandler<javafx.event.ActionEvent> onFinished) {
            animation.setOnFinished(onFinished);
            animation.setNode(node);
            animation.playFromStart();
        }

    }

}
