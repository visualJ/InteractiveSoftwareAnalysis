package interactivesoftwareanalysis.modules.data;

import interactivesoftwareanalysis.modules.filter.Filter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A table data structure that can be provided by modules and consumed by the ui
 */
@Data @AllArgsConstructor @RequiredArgsConstructor
public class DataTable implements DataType {
    private final List<DataItem> table;
    @Setter private List<String> columns;

    /**
     * Retrieve this data structures content as a list of data items while filtering it
     * @param filter the filter to use on the data
     * @return a filtered list of data items in this data list
     */
    public List<DataItem> getFilteredTable(Filter filter) {
        if (filter != null){
            return getTable().stream().parallel().filter(filter::filter).collect(Collectors.toList());
        }else{
            return getTable();
        }
    }

    /**
     * Retrieve the list of data items in this data type
     * @return
     */
    public List<DataItem> getTable(){
        // set the tags
        table.forEach(dataItem -> {
            if (dataItem.getResource() != null) {
                dataItem.setTags(dataItem.getResource().getTags());
            }
        });
        return table;
    }

    /**
     * Retrieve the columns of the table.
     * If the columns aren't set yet, column names are retrieved from the data and sorted lexicographically.
     * @return a list of column names
     */
    public List<String> getColumns() {
        return columns != null ? columns : table.stream()
                .flatMap(dataItem -> dataItem.getStrings().keySet().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
