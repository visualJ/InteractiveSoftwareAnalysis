package testpackage;

import interactivesoftwareanalysis.model.ResourceNotFoundException;
import interactivesoftwareanalysis.modules.InteractiveSubmoduleBase;
import interactivesoftwareanalysis.modules.ModuleBase;
import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;
import interactivesoftwareanalysis.modules.data.DataList;
import interactivesoftwareanalysis.modules.data.DataTypeNotSupportedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by benedikt.ringlein on 24.08.2016.
 */
public class TestModule extends ModuleBase {

    public TestModule(ModuleContext moduleContext) {
        super(moduleContext, "Test", "Hier wird getestet.");
        getInteractiveSubmodules().addAll(Collections.singletonList(
                new TestSubmodule(moduleContext)
        ));
    }

    private class TestSubmodule extends InteractiveSubmoduleBase {

        public TestSubmodule(ModuleContext moduleContext) {
            super("Test-Ressourcen", "Zeigt alle im Modell enthaltenen Ressourcen an, testweise", Collections.singletonList(DataList.class), moduleContext);
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
            } else {
                throw new DataTypeNotSupportedException(this.getClass().toString() + " does not support datatype " + dataType.toString());
            }
        }
    }
}
