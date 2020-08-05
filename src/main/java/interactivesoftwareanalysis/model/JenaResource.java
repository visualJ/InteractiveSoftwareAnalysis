package interactivesoftwareanalysis.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.Lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A resource implementation for the {@link JenaRDFModel}
 */
@RequiredArgsConstructor @ToString @EqualsAndHashCode public class JenaResource implements Resource {
    @Getter private final String uri;
    @Getter private final Model model;
    private final org.apache.jena.rdf.model.Resource resource;

    @Override public List<Attribute> getAttributes() {
        List<Attribute> attributeList = new ArrayList<>();
        List<Map<String, String>> results = model.executeSelectQuery("SELECT ?p ?o WHERE {<" + uri + "> ?p ?o.}");
        results.forEach(
                querySolution -> attributeList.add(new JenaAttribute(model, querySolution.get("o"), this)));
        return attributeList;
    }

    @Override public List<Attribute> getAttributes(String attributeTypeUri) {
        List<Attribute> attributeList = new ArrayList<>();
        List<Map<String, String>> results = model.executeSelectQuery("SELECT ?o WHERE {<" + uri + "> <" + attributeTypeUri + "> ?o.}");
        results.forEach(
                querySolution -> attributeList.add(new JenaAttribute(model, querySolution.get("o"), this)));
        return attributeList;
    }

    @Override public void addAttribute(String attributeTypeUri, String value) {
        try {
            model.addAttribute(uri, attributeTypeUri, value);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override public void addResourceAttribute(String attributeTypeUri, String valueUri) {
        try {
            model.addResourceAttribute(uri, attributeTypeUri, valueUri);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override public List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        org.apache.jena.rdf.model.Model model = resource.getModel();
        model.enterCriticalSection(Lock.READ);
        org.apache.jena.rdf.model.NodeIterator iterator = model.listObjectsOfProperty(resource, model.createProperty("http://interactivesoftwareanalysis/tag"));
        while(iterator.hasNext()){
           RDFNode node = iterator.next();
            if (node.isAnon()){
                String tagName = node.asResource().getProperty(model.createProperty("http://interactivesoftwareanalysis/tagName")).getString();
                String tagDetail = node.asResource().getProperty(model.createProperty("http://interactivesoftwareanalysis/tagDetail")).getString();
                tags.add(new Tag(tagName, tagDetail));
            }
        }
        model.leaveCriticalSection();
        tags.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        return tags;
    }

    @Override public void addTag(String tagName, String tagDetail) {
        String tagTTL = String.format("<%s> isa:tag [isa:tagName '%s'; isa:tagDetail '%s'].\n", uri, tagName, tagDetail);
        model.insert(tagTTL);
    }
}
