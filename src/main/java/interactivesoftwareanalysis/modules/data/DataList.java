package interactivesoftwareanalysis.modules.data;

import interactivesoftwareanalysis.modules.filter.Filter;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A list data structure that can be provided by modules and consumed by the ui
 */
@Data
public class DataList implements DataType {
    private final List<DataItem> list;

    /**
     * Retrieve this data structures content as a list of data items while filtering it
     * @param filter the filter to use on the data
     * @return a filtered list of data items in this data list
     */
    public List<DataItem> getFilteredList(Filter filter) {
        if (filter != null){
            return getList().stream().parallel().filter(filter::filter).collect(Collectors.toList());
        }else{
            return getList();
        }
    }

    /**
     * Retrieve the list of data items in this data type
     * @return
     */
    public List<DataItem> getList(){
        // set the tags
        list.forEach(dataItem -> {
            if (dataItem.getResource() != null) {
                dataItem.setTags(dataItem.getResource().getTags());
            }
        });
        return list;
    }
}
