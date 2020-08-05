package interactivesoftwareanalysis.modules.data;

import interactivesoftwareanalysis.model.Resource;
import interactivesoftwareanalysis.model.Tag;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An item that contains data produced by modules for display in the UI.
 * Data items can contain one or multiple string value and an optional resource.
 */
@Data public class DataItem {
    private Resource resource;
    @NonNull private String string;
    @NonNull private Map<String, String> strings = new HashMap<>();
    @NonNull private List<Tag> tags = new ArrayList<>();

    /**
     * Create a new data item with a non-null string value and an optional resource (may be null)
     * @param string a string that acts as a display value for this item
     * @param resource the resource that this item describes, if applicable, or null.
     */
    public DataItem(@NonNull String string, Resource resource) {
        this.string = string;
        this.resource = resource;
    }

    @Override public String toString(){
        if (!tags.isEmpty()){
            return string + " (" + tags.stream().map(Tag::getName).collect(Collectors.joining(", ")) + ")";
        }
        return string;
    }
}
