package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.InteractiveSubmodule;
import interactivesoftwareanalysis.modules.ModuleManager;
import interactivesoftwareanalysis.modules.data.DataItem;
import interactivesoftwareanalysis.modules.data.DataTable;
import interactivesoftwareanalysis.modules.data.DataType;
import interactivesoftwareanalysis.modules.data.DataTypeNotSupportedException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.SneakyThrows;

import java.util.List;

/**
 * A view that displays {@link DataTable}s.
 */
class UITableView extends UIView {

    private static final Class<DataTable> ACCEPTED_DATA_TYPE = DataTable.class;
    private TableView<DataItem> tableView;
    private DataTable dataTable;

    /**
     * Retrieves a factory for instantiating this view type
     * @param uiManager the ui manager to create the view with
     * @param moduleManager the module manager to create the view with
     * @return a factory for this view type
     */
    public static UIViewFactory getFactory(UIManager uiManager, ModuleManager moduleManager){
        return new UIViewFactoryBase("Tabelle", uiManager, moduleManager) {
            @Override public Class<? extends DataType> getSupportedDataType() {
                return ACCEPTED_DATA_TYPE;
            }

            @Override public UIView newInstance(InteractiveSubmodule interactiveSubmodule) {
                return new UITableView(interactiveSubmodule, uiManager, moduleManager);
            }
        };
    }

    public UITableView(InteractiveSubmodule interactiveSubmodule, UIManager uiManager, ModuleManager moduleManager) {
        super(interactiveSubmodule, ACCEPTED_DATA_TYPE, uiManager, moduleManager);
        tableView = new TableView<>();
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setContextMenu(contextMenu);
        tableView.setTableMenuButtonVisible(true);
        setContent(tableView);

        selectionProperty().set(tableView.getSelectionModel().getSelectedItems());
    }

    @Override @SneakyThrows(DataTypeNotSupportedException.class)
    public void update() {
        setProgressVisible(true);
        dataTable = interactiveSubmodule.getData(DataTable.class);
        Platform.runLater(() -> {
                    tableView.getColumns().clear();
                    for (String s : dataTable.getColumns()) {
                        TableColumn<DataItem, String> column = new TableColumn<>(s);
                        column.setCellValueFactory(param -> new ObservableValueBase<String>() {
                            @Override
                            public String getValue() {
                                return param.getValue().getStrings().getOrDefault(s, "");
                            }
                        });
                        Platform.runLater(() -> tableView.getColumns().add(column));
                    }
                });
        updateFilter();
    }

    @Override public void updateFilter() {
        new Thread(() -> {
            setProgressVisible(true);
            List<DataItem> list = dataTable.getFilteredTable(filter);
            setProgressVisible(false);
            Platform.runLater(() -> tableView.setItems(FXCollections.observableArrayList(list)));
        }).start();
    }

}
