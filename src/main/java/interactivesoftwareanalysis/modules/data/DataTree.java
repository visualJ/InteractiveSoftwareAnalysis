package interactivesoftwareanalysis.modules.data;

import interactivesoftwareanalysis.modules.filter.Filter;
import javafx.scene.control.TreeItem;
import lombok.Data;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * A tree data structure that can be provided by modules and consumed by the ui
 */
@Data
public class DataTree implements DataType {

    private final TreeItem<DataItem> root;

    /**
     * Traverse data items in this tree in pre oder
     * @param visitor the visitor to visit data items with
     */
    public void traverseItemsPreOrder(@NonNull Consumer<DataItem> visitor) {
        traverseTree(root, treeItem -> visitor.accept(treeItem.getValue()) , null, null);
    }

    /**
     * Traverse the tree and execute function at dirrerent stages of the traversal.
     * @param item the tree root
     * @param preOrder a function to execute in pre order
     * @param inOrder a function to execute in order
     * @param postOrder a function to execute in post order
     */
    private void traverseTree(TreeItem<DataItem> item, Consumer<TreeItem<DataItem>> preOrder, Consumer<TreeItem<DataItem>> inOrder, Consumer<TreeItem<DataItem>> postOrder) {
        if (preOrder != null) {
            preOrder.accept(item);
        }
        for (TreeItem<DataItem> dataItemTreeItem : item.getChildren()) {
            traverseTree(dataItemTreeItem, preOrder, inOrder, postOrder);
            if (inOrder != null) {
                inOrder.accept(item);
            }
        }
        if (postOrder != null) {
            postOrder.accept(item);
        }
    }

    /**
     * Decide, weather a tree item or one of its children matches with a filter
     * @param treeItem the tree item to check for a match
     * @param filter the filter to use
     * @return true, iff the filter returns true for the data item in the tree item or one of its children.
     */
    private boolean matches(TreeItem<DataItem> treeItem, Filter filter) {
        if (filter.filter(treeItem.getValue())){
            return true;
        }
        for (TreeItem<DataItem> child : treeItem.getChildren()) {
            if (matches(child, filter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copy the tree while filtering it.
     * Only the tree items are copied, not the data items. Data items are identical in original and copy.
     * @param treeItem the root tree item
     * @param filter the filter to use for deciding, if a tree item ist filtered or not
     * @return the root item of a tree of copied items.
     */
    private TreeItem<DataItem> copyFiltered(@NonNull TreeItem<DataItem> treeItem, @NonNull Filter filter) {
        TreeItem<DataItem> newTreeItem = new TreeItem<>(treeItem.getValue());
        newTreeItem.setExpanded(treeItem.isExpanded());
        treeItem.getChildren().stream()
                .filter(child -> matches(child, filter))
                .forEach(child -> newTreeItem.getChildren().add(copyFiltered(child, filter)));
        return newTreeItem;
    }

    /**
     * Retrieve the root of the tree in this data type
     * @return the root tree node
     */
    public TreeItem<DataItem> getRoot(){
        // set the tags
        traverseItemsPreOrder(dataItem -> {
            if (dataItem.getResource() != null) {
                dataItem.setTags(dataItem.getResource().getTags());
            }
        });
        return root;
    }

    /**
     * Retrieve a copy of this data structures content as a tree of data items while filtering it.
     * This preserved the expanded state of the original tree structure.
     * @param filter the filter to use on the data
     * @return a filtered tree (actually, its root item) of data items in this data list
     */
    public TreeItem<DataItem> getFilteredRoot(Filter filter){
        if (filter != null){
            return copyFiltered(getRoot(), filter);
        }else{
            return getRoot();
        }
    }
}
