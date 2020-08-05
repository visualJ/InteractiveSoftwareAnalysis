package interactivesoftwareanalysis.model;

import com.google.common.eventbus.EventBus;
import interactivesoftwareanalysis.model.events.ModelChangedEvent;
import lombok.Getter;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.Lock;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>A model implementation that uses a Jena model as an internal representation.</p>
 *
 * <p>ModelChangedEvents are posted on the event bus, when the model changes.</p>
 *
 * <p>Some prefixes are automatically added to queries: isa and rdf</p>
 */
public class JenaRDFModel implements Model {

    private static final String knownPrefixes = "PREFIX isa: <http://interactivesoftwareanalysis/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    @Getter private org.apache.jena.rdf.model.Model internalModel;

    private boolean batchMode = false;
    private EventBus eventBus;

    /**
     * Create a new, empty model.
     * @param eventBus the event bus to post change event on
     */
    @Inject public JenaRDFModel(EventBus eventBus) {
        this.eventBus = eventBus;
        internalModel = ModelFactory.createDefaultModel();
        internalModel.setNsPrefix("isa", "http://interactivesoftwareanalysis/");
    }

    @Override public List<Map<String, String>> executeSelectQuery(String query) {
        internalModel.enterCriticalSection(Lock.READ);
        ResultSet resultSet = executeSelectQuery(QueryFactory.create(knownPrefixes + " " + query.trim()));
        List<Map<String, String>> results = new ArrayList<>();
        for (QuerySolution solution : (Iterable<QuerySolution>) () -> resultSet){
            Map<String, String> result = new HashMap<>();
            for (String variable : resultSet.getResultVars()) {
                result.put(variable, solution.get(variable).toString());
            }
            results.add(result);
        }
        internalModel.leaveCriticalSection();
        return results;
    }

    @Override public boolean executeAskQuery(String query) {
        internalModel.enterCriticalSection(Lock.READ);
        boolean result = executeAskQuery(QueryFactory.create(knownPrefixes + " " + query.trim()));
        internalModel.leaveCriticalSection();
        return result;
    }

    @Override public void load(InputStream stream) {
        internalModel.enterCriticalSection(Lock.WRITE);
        internalModel.removeAll();
        internalModel.read(stream, "");
        internalModel.leaveCriticalSection();
        fireModelChangedEvent();
    }

    @Override public void save(OutputStream stream) {
        internalModel.enterCriticalSection(Lock.READ);
        internalModel.write(stream);
        internalModel.leaveCriticalSection();
    }

    @Override public List<Resource> getResources() {
        List<Resource> resourceList = new ArrayList<>();
        List<Map<String, String>> results = executeSelectQuery("SELECT DISTINCT ?s WHERE {?s ?p ?o.}");
        results.forEach(querySolution -> {
            String uri = querySolution.get("?s");
            resourceList.add(new JenaResource(uri, this, internalModel.createResource(uri)));
        });
        return resourceList;
    }

    @Override public Resource getResource(String uri) throws ResourceNotFoundException {
        org.apache.jena.rdf.model.Resource resource = internalModel.createResource(uri);
        if (internalModel.contains(resource, null)) {
            return new JenaResource(uri, this, resource);
        } else {
            throw new ResourceNotFoundException("Resource " + uri + " was not in the model");
        }
    }

    @Override public Resource createResource(String resourceURI, String typeURI, String humanReadableName) {
        internalModel.enterCriticalSection(Lock.WRITE);
        org.apache.jena.rdf.model.Resource resource = internalModel.createResource(resourceURI, internalModel.createResource(typeURI));
        Property property = internalModel.createProperty("http://interactivesoftwareanalysis/humanReadableName");
        if (humanReadableName == null || humanReadableName.isEmpty()) {
            humanReadableName = resourceURI;
        }
        resource.addProperty(property, humanReadableName);
        internalModel.leaveCriticalSection();
        fireModelChangedEvent();
        return new JenaResource(resourceURI, this, resource);
    }

    @Override
    public Attribute addAttribute(String resourceUri, String attributeTypeUri, String value) throws ResourceNotFoundException {
        internalModel.enterCriticalSection(Lock.WRITE);
        Attribute attribute = new JenaAttribute(this, value, getResource(resourceUri));
        org.apache.jena.rdf.model.Resource resource = internalModel.createResource(resourceUri);
        Property property = internalModel.createProperty(attributeTypeUri);
        RDFNode literal = value != null ? internalModel.createLiteral(value) : internalModel.createResource();
        internalModel.add(internalModel.createStatement(resource, property, literal));
        internalModel.leaveCriticalSection();
        fireModelChangedEvent();
        return attribute;
    }

    @Override public Attribute addResourceAttribute(String resourceUri, String attributeTypeUri, String valueUri) throws ResourceNotFoundException {
        internalModel.enterCriticalSection(Lock.WRITE);
        Attribute attribute = new JenaAttribute(this, valueUri, getResource(resourceUri));
        org.apache.jena.rdf.model.Resource resource = internalModel.createResource(resourceUri);
        Property property = internalModel.createProperty(attributeTypeUri);
        org.apache.jena.rdf.model.Resource valueResource = internalModel.createResource(valueUri);
        internalModel.add(internalModel.createStatement(resource, property, valueResource));
        internalModel.leaveCriticalSection();
        fireModelChangedEvent();
        return attribute;
    }

    @Override public void setBatchMode(boolean batchMode) {
        boolean oldBatchMode = this.batchMode;
        this.batchMode = batchMode;
        if (oldBatchMode && !batchMode) {
            eventBus.post(new ModelChangedEvent());
        }
    }

    @Override public boolean isBatchMode() {
        return batchMode;
    }

    @Override public void insert(String data) {
        org.apache.jena.rdf.model.Model newModel = ModelFactory.createDefaultModel();
        InputStream inputStream = new ByteArrayInputStream((knownPrefixes + " " + data).getBytes());
        newModel.read(inputStream, "", "TTL");
        internalModel.add(newModel);
        fireModelChangedEvent();
    }

    @Override public void removeStatements(String resourceURI, String propertyURI) {
        internalModel.enterCriticalSection(Lock.WRITE);
        org.apache.jena.rdf.model.Resource resource = internalModel.createResource(resourceURI);
        Property property = internalModel.createProperty(propertyURI);
        StmtIterator statementIterator = internalModel.listStatements(resource, property, (RDFNode) null);
        List<Statement> statements = new ArrayList<>();
        while (statementIterator.hasNext()) {
            statements.add(statementIterator.next());
        }
        statements.forEach(Statement::remove);
        internalModel.leaveCriticalSection();
        fireModelChangedEvent();
    }

    /**
     * Execute a select query from a query object and return the result
     * @param query the query object containing the select query
     * @return a result set with results from the query
     */
    private ResultSet executeSelectQuery(Query query) {
        QueryExecution queryExecution = QueryExecutionFactory.create(query, internalModel);
        return queryExecution.execSelect();
    }

    /**
     * Execute an ask query from a query object and return the result
     * @param query the query object containing the ask query
     * @return the query result
     */
    private boolean executeAskQuery(Query query) {
        QueryExecution queryExecution = QueryExecutionFactory.create(query, internalModel);
        return queryExecution.execAsk();
    }

    /**
     * Post a model changed event on the event bus if batch mode is off
     */
    private void fireModelChangedEvent() {
        if (!isBatchMode()) {
            eventBus.post(new ModelChangedEvent());
        }
    }

}
