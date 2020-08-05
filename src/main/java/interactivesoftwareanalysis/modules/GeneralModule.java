package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.model.ResourceNotFoundException;
import interactivesoftwareanalysis.modules.data.*;
import interactivesoftwareanalysis.modules.filter.*;
import javafx.scene.control.TreeItem;

import java.text.MessageFormat;
import java.util.*;

/**
 * A module for general, not domain specific, Elements.
 *
 * <p>
 *     Interactive submodules:
 *     <ul>
 *         <li>{@link ResourcesSubmodule}</li>
 *         <li>{@link TaggedResourcesSubmodule}</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     Filters:
 *     <ul>
 *         <li>name</li>
 *         <li>text</li>
 *         <li>tag</li>
 *         <li>sparql</li>
 *         <li>regex</li>
 *         <li>uri</li>
 *     </ul>
 * </p>
 */
public class GeneralModule extends ModuleBase {

    public GeneralModule(ModuleContext moduleContext) {
        super(moduleContext, "Allgemein", "Dieses Modul stellt allgemeine Informationen bereit.");
        getInteractiveSubmodules().addAll(Arrays.asList(
                new ResourcesSubmodule(moduleContext),
                new TaggedResourcesSubmodule(moduleContext)
        ));
        getFilterFactories().addAll(Arrays.asList(
                new DecideFilterFactoryBase("name", "Filtert nach Items, deren Name den Text enthält") {
                    @Override
                    public Filter newInstance(String pattern) {
                        return new NameFilter(pattern);
                    }
                },
                new DecideFilterFactoryBase("text", "Filtert nach Items, die den angegebenen Text enthalten") {
                    @Override
                    public Filter newInstance(String pattern) {
                        return new TextFilter(pattern);
                    }
                },
                new DecideFilterFactoryBase("tag", "Filtert nach Items, die mit exakt diesem Tag getaggt sind.") {
                    @Override
                    public Filter newInstance(String pattern) {
                        return new TagFilter(pattern);
                    }
                },
                new DecideFilterFactoryBase("sparql", "Filtert nach Items, die dieser SPARQL Query entsprechen.", moduleContext) {
                    @Override
                    public Filter newInstance(String pattern) {
                        return new SparqlFilter(pattern, moduleContext);
                    }
                },
                new DecideFilterFactoryBase("regex", "Filtert nach Items, die dem regulären Ausdruck entsprechen.") {
                    @Override
                    public Filter newInstance(String pattern) {
                        return new RegexFilter(pattern);
                    }
                },
                new DecideFilterFactoryBase("uri", "Filtert nach der Ressource mit diesem URI") {
                    @Override
                    public Filter newInstance(String pattern) {
                        return new URIFilter(pattern);
                    }
                }
        ));
    }

    /**
     * Retrieves all resources as list or table
     */
    private class ResourcesSubmodule extends InteractiveSubmoduleBase {

        public ResourcesSubmodule(ModuleContext moduleContext) {
            super("Ressourcen", "Zeigt alle im Modell enthaltenen Ressourcen an", Arrays.asList(DataList.class, DataTable.class), moduleContext);
        }

        @Override public <T> T getData(Class<T> dataType) throws DataTypeNotSupportedException {
            if (dataType == DataList.class) {
                String query = "SELECT DISTINCT ?uri ?name WHERE { ?uri ?p ?o. ?uri isa:humanReadableName ?name. }";
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                List<DataItem> tags = new ArrayList<>();
                results.forEach(solution -> {
                    try {
                        tags.add(new DataItem(solution.get("name"), moduleContext.getModel().getResource(solution.get("uri"))));
                    } catch (ResourceNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                tags.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataList(tags);
                return data;
            } else if (dataType == DataTable.class) {
                Map<String, DataItem> items = new HashMap<>();
                String query = "SELECT ?uri ?property ?value  WHERE { ?uri ?property ?value. }";
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                results.forEach(solution -> {
                    try {
                        String uri = solution.get("uri");
                        String property = solution.get("property");
                        String value = solution.get("value");
                        DataItem item = null;
                        if (items.containsKey(uri)) {
                            item = items.get(uri);
                        } else {
                            item = new DataItem(uri, moduleContext.getModel().getResource(uri));
                            items.put(uri, item);
                            item.getStrings().put("URI", uri);
                        }
                        String propertyDisplayName = getPropertyDisplayName(property);
                        String previousValue = item.getStrings().getOrDefault(propertyDisplayName, "");
                        item.getStrings().put(propertyDisplayName, (previousValue.isEmpty() ? "" : previousValue + ", ") +  value);
                    } catch (ResourceNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                ArrayList<DataItem> itemList = new ArrayList<>(items.values());
                itemList.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                T data = (T) new DataTable(itemList);
                return data;
            } else {
                throw new DataTypeNotSupportedException(this.getClass().toString() + " does not support datatype " + dataType.toString());
            }
        }

        /**
         * Create a display name for a property by prepending the last uri segment
         * @param property the property uri to create adiplay name for
         * @return a short property name with the full uri in braces
         */
        private String getPropertyDisplayName(String property) {
            String[] slashSplit = property.split("/");
            String[] numberSignSplit = property.split("#");
            String lastSlashString = slashSplit[slashSplit.length - 1];
            String lastNumberSignString = numberSignSplit[numberSignSplit.length - 1];
            String displayName = property.contains("#") ? lastNumberSignString : lastSlashString;
            if (!property.contains("#") && !property.contains("/")) {
                return property;
            } else {
                return MessageFormat.format("{0}  ({1})", displayName, property);
            }
        }
    }

    /**
     * Retrieves tagged resources as list or tree
     */
    private class TaggedResourcesSubmodule extends InteractiveSubmoduleBase {

        public TaggedResourcesSubmodule(ModuleContext moduleContext) {
            super("Getaggte Resourcen", "Zeigt Ressourcen an, die Tags haben", Arrays.asList(DataList.class, DataTree.class), moduleContext);
        }

        @Override public <T> T getData(Class<T> dataType) throws DataTypeNotSupportedException {
            if (dataType == DataList.class) {
                String query = "SELECT DISTINCT ?uri ?name WHERE { ?uri isa:humanReadableName ?name. ?uri isa:tag [isa:tagName ?tagName; isa:tagDetail ?tagDetail].}";
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                List<DataItem> tags = new ArrayList<>();
                results.forEach(result -> {
                    String name = result.get("name");
                    String uri = result.get("uri");
                    try {
                        tags.add(new DataItem(name, moduleContext.getModel().getResource(uri)));
                    } catch (ResourceNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                tags.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataList(tags);
                return data;
            } else if (dataType == DataTree.class) {
                Map<String, TreeItem<DataItem>> tags = new HashMap<>();
                String query = "SELECT DISTINCT ?uri ?name ?tagName WHERE { ?uri isa:tag [isa:tagName ?tagName; isa:tagDetail ?tagDetail]. ?uri isa:humanReadableName ?name}";
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                results.forEach(solution -> {
                    String tagName = solution.get("tagName");
                    String uri = solution.get("uri");
                    String name = solution.get("name");
                    TreeItem<DataItem> tagItem;
                    if (tags.containsKey(tagName)){
                        tagItem = tags.get(tagName);
                    }else{
                        tagItem = new TreeItem<>(new DataItem(tagName, null));
                        tags.put(tagName, tagItem);
                    }
                    try {
                        tagItem.getChildren().add(new TreeItem<>(new DataItem(name, moduleContext.getModel().getResource(uri))));
                    } catch (ResourceNotFoundException e) {
                        // if the resource is not there anymore, just don't add it.
                    }
                });

                TreeItem<DataItem> rootItem = new TreeItem<>(new DataItem("Tags", null));
                List<TreeItem<DataItem>> values = new ArrayList<>(tags.values());
                values.sort((o1, o2) -> o1.getValue().getString().compareToIgnoreCase(o2.getValue().getString()));
                values.forEach(item -> item.getChildren().sort((o1, o2) -> o1.getValue().getString().compareToIgnoreCase(o2.getValue().getString())));
                rootItem.getChildren().addAll(values);
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataTree(rootItem);
                return data;
            } else {
                throw new DataTypeNotSupportedException(this.getClass().toString() + " does not support datatype " + dataType.toString());
            }
        }
    }
}
