package interactivesoftwareanalysis.modules.actions;

import interactivesoftwareanalysis.modules.ModuleContext;
import interactivesoftwareanalysis.modules.data.DataItem;
import interactivesoftwareanalysis.modules.parameters.StringParameter;
import interactivesoftwareanalysis.modules.parameters.VisitParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A module action that adds tags to selected resources
 */
public class TagModuleAction extends ResourceSelectionModuleAction {

    @VisitParameter private StringParameter tagName;
    @VisitParameter private StringParameter tagDetail;

    public TagModuleAction(ModuleContext moduleContext) {
        super("Mit Tag versehen", "Taggt Ressourcen", moduleContext);
        tagName = new StringParameter("Tagname", "Die Bezeichnung des Tags. "
                + "Kann ein neuer oder ein bereits verwendeter Tag sein. "
                + "Mehrere Tags können durch Komma getrennt angegeben werden.", () -> "", this::getTagNames);
        tagName.setList(true);
        tagName.setSeparator(",");
        tagDetail = new StringParameter("Details / Begründung", "Details zu diesem Tag, Z.B eine Begründung, warum dieser Tag hier gesetzt wurde");
        tagDetail.setMultiline(true);
    }

    /**
     * Retrieve a list of tag names already used in the model
     * @return a list of tag names
     */
    private List<String> getTagNames() {
        String query = "SELECT DISTINCT ?tagName WHERE { ?thing isa:tag [isa:tagName ?tagName; isa:tagDetail ?tagDetail]}";
        List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
        List<String> tags = new ArrayList<>();
        results.forEach(solution -> tags.add(solution.get("tagName")));
        return tags;
    }

    @Override public void execute(List<DataItem> input) {
        moduleContext.getModel().setBatchMode(true);
        input.stream()
                .map(DataItem::getResource)
                .filter(resource -> resource != null)
                .forEach(resource -> {
                    for (String tagNameString : tagName.getValues()) {
                        if (!tagNameString.isEmpty()) {
                            resource.addTag(tagNameString.trim(), tagDetail.getValue());
                        }
                    }
                });
        moduleContext.getModel().setBatchMode(false);
    }
}
