package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.modules.data.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A view that displays {@link DataTree}s.
 */
class UITreeView extends UIView {

    private static final Class<DataTree> ACCEPDTED_DATA_TYPE = DataTree.class;
    private TreeView<DataItem> treeView;
    private DataTree dataTree;
    private TreeItem<DataItem> filteredRoot;

    /**
     * Retrieves a factory for instantiating this view type
     * @param uiManager the ui manager to create the view with
     * @param moduleManager the module manager to create the view with
     * @return a factory for this view type
     */
    public static UIViewFactory getFactory(UIManager uiManager, ModuleManager moduleManager){
        return new UIViewFactoryBase("Baum", uiManager, moduleManager) {
            @Override public Class<? extends DataType> getSupportedDataType() {
                return ACCEPDTED_DATA_TYPE;
            }

            @Override public UIView newInstance(InteractiveSubmodule interactiveSubmodule) {
                return new UITreeView(interactiveSubmodule, uiManager, moduleManager);
            }
        };
    }

    public UITreeView(InteractiveSubmodule interactiveSubmodule, UIManager uiManager, ModuleManager moduleManager) {
        super(interactiveSubmodule, ACCEPDTED_DATA_TYPE, uiManager, moduleManager);
        this.treeView = new TreeView<>();
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeView.setContextMenu(contextMenu);
        setContent(treeView);

        // set the cell factory to display tags
        treeView.setCellFactory(param -> {
            TagBox tagBox = new TagBox();
            tagBox.setPadding(new Insets(0, 0, 0, 10));
            TreeCell<DataItem> cell = new TreeCell<>();
            cell.setGraphic(tagBox);
            cell.setContentDisplay(ContentDisplay.RIGHT);
            cell.itemProperty().addListener((observable, oldValue, newValue) -> {
                tagBox.setTags(null);
                if (newValue != null) {
                    cell.setText(newValue.getString());
                    tagBox.setTags(newValue.getTags());
                }else{
                    cell.setText("");
                }
            });
            return cell;
        });

        selectionProperty().bind(Bindings.createObjectBinding(() -> {
            return FXCollections.observableArrayList(treeView.getSelectionModel().getSelectedItems().stream()
                    .map(TreeItem::getValue)
                    .collect(Collectors.toList()));
        }, treeView.getSelectionModel().getSelectedItems()));
    }

    @Override @SneakyThrows(DataTypeNotSupportedException.class)
    public void update() {
        setProgressVisible(true);
        dataTree = interactiveSubmodule.getData(DataTree.class);
        updateFilter();
    }

    @Override public void updateFilter() {
        new Thread(() -> {
            TreeItem<DataItem> oldFilteredRoot = filteredRoot;
            setProgressVisible(true);
            filteredRoot = dataTree.getFilteredRoot(filter);
            if (oldFilteredRoot != null) {
                copyExpandedState(oldFilteredRoot, filteredRoot);
            }
            setProgressVisible(false);
            Platform.runLater(() -> {
                treeView.setRoot(filteredRoot);
                dataTree.getRoot().setExpanded(true);
            });
        }).start();
    }

    /**
     * Copy the expanded state from one tree item and its children to another tree item and its children
     * @param from tree item to copy the expanded state from
     * @param to tree item to copy the expanded state to
     */
    private void copyExpandedState(TreeItem<DataItem> from, TreeItem<DataItem> to){
        to.setExpanded(from.isExpanded());
        for (TreeItem<DataItem> toChild : to.getChildren()) {
            Optional<TreeItem<DataItem>> fromChild = from.getChildren().stream().filter(fromItem -> fromItem.getValue().equals(toChild.getValue())).findAny();
            if (fromChild.isPresent()) {
                copyExpandedState(fromChild.get(), toChild);
            }
        }
    }
}
