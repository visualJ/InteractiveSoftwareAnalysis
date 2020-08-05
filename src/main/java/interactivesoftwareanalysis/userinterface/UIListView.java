package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.modules.data.DataItem;
import interactivesoftwareanalysis.modules.data.DataList;
import interactivesoftwareanalysis.modules.data.DataType;
import interactivesoftwareanalysis.modules.data.DataTypeNotSupportedException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import lombok.SneakyThrows;

import java.util.List;

/**
 * A view that displays {@link DataList}s
 */
class UIListView extends UIView {

    private static final Class<DataList> ACCEPTED_DATA_TYPE = DataList.class;
    private ListView<DataItem> listView;
    private DataList dataList;

    /**
     * Retrieves a factory for instantiating this view type
     * @param uiManager the ui manager to create the view with
     * @param moduleManager the module manager to create the view with
     * @return a factory for this view type
     */
    public static UIViewFactory getFactory(UIManager uiManager, ModuleManager moduleManager){
        return new UIViewFactoryBase("Liste", uiManager, moduleManager) {
            @Override public Class<? extends DataType> getSupportedDataType() {
                return ACCEPTED_DATA_TYPE;
            }

            @Override public UIView newInstance(InteractiveSubmodule interactiveSubmodule) {
                return new UIListView(interactiveSubmodule, uiManager, moduleManager);
            }
        };
    }

    public UIListView(InteractiveSubmodule interactiveSubmodule, UIManager uiManager, ModuleManager moduleManager) {
        super(interactiveSubmodule, ACCEPTED_DATA_TYPE, uiManager, moduleManager);
        listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setContextMenu(contextMenu);
        setContent(listView);

        listView.setCellFactory(param -> {
            TagBox tagBox = new TagBox();
            tagBox.setPadding(new Insets(0, 0, 0, 10));
            ListCell<DataItem> cell = new ListCell<>();
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

        selectionProperty().set(listView.getSelectionModel().getSelectedItems());
    }

    @Override @SneakyThrows(DataTypeNotSupportedException.class)
    public void update() {
        setProgressVisible(true);
        dataList = interactiveSubmodule.getData(DataList.class);
        updateFilter();
    }

    @Override public void updateFilter() {
        new Thread(() -> {
            setProgressVisible(true);
            List<DataItem> list = dataList.getFilteredList(filter);
            setProgressVisible(false);
            Platform.runLater(() -> listView.setItems(FXCollections.observableArrayList(list)));
        }).start();
    }

}
