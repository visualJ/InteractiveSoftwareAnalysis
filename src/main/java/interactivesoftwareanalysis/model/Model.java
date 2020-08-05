package interactivesoftwareanalysis.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * A model represents all data that is available for display, analysis or processing.
 * It is also a central element for modules. They import, export and work with the data in the model.
 *
 * A model contains {@link Resource} objects that can have
 * any number of {@link Attribute}s. Those objects can be used to get data from the model
 * or to manipulate it.
 *
 */
public interface Model {

    /**
     * Execute a SPARQL select query and return the result
     * @param query the full SPARQL select query
     * @return a List of results. Each result is a map with the
     *  result variables as key/value pairs
     */
    List<Map<String, String>> executeSelectQuery(String query);

    /**
     * Execute a SPARQL ask query and return the result
     * @param query the full SPARQL ask query
     * @return true, iff thestatements in the ask query are true.
     */
    boolean executeAskQuery(String query);

    /**
     * Loads a model from a file (rdf/xml).
     * @param stream stream to load the model from
     */
    void load(InputStream stream);

    /**
     * Saves a model to a file (rdf/xml).
     * @param stream stream to save the model to
     */
    void save(OutputStream stream);

    /**
     * Get all resources in this model.
     * @return a list of resource objects this model contains
     */
    List<Resource> getResources();

    /**
     * Get a resource with a specific uri.
     * @param uri the uri of the resource, including namespace.
     * @return a resource object representing the resource.
     * @throws ResourceNotFoundException when no resources with the requested uri was found in the model
     */
    Resource getResource(String uri) throws ResourceNotFoundException;

    /**
     * Create a resurce and insert it into the model.
     * @param resourceURI the uri of the new resource
     * @param typeURI the type uri of the resource
     * @param humanReadableName a human readable name for displaying the resource
     * @return a resource object for further operations
     */
    Resource createResource(String resourceURI, String typeURI, String humanReadableName);

    /**
     * Add an attribute to a resource
     * @param resourceUri the resources uri
     * @param attributeTypeUri the uri of the attribute type to add
     * @param value the attribute value
     * @return an attribute objec to perform further operations on
     * @throws ResourceNotFoundException if the resource does not exist in the model
     */
    Attribute addAttribute(String resourceUri, String attributeTypeUri, String value) throws ResourceNotFoundException;

    /**
     * Add a resource attribute to a resource
     * @param resourceUri the resources uri
     * @param attributeTypeUri the uri of the attribute type to add
     * @param valueUri the attribute value uri
     * @return an attribute objec to perform further operations on
     * @throws ResourceNotFoundException if the resource does not exist in the model
     */
    Attribute addResourceAttribute(String resourceUri, String attributeTypeUri, String valueUri) throws ResourceNotFoundException;

    /**
     * Set the batch mode.
     * The batch mode disables model change events, that are otherwise fired after every model change.
     * Deactivating the batch mode enables that again and immediately fires a change event.
     * This should be activated before adding or modifying many resources and deactivated afterwards.
     * @param batchMode true to activate the batch mode, false to deactivate it.
     */
    void setBatchMode(boolean batchMode);

    /**
     * Return the current batch mode state. See <code>setBatchMode(boolean batchMode)</code> for further details.
     * @return true, iff batch mode is currently active.
     */
    boolean isBatchMode();

    /**
     * Insert resources and attributes into the model.
     * Resources and attributes are described using a Turtle string.
     * You can assume the namespaces isa: and rdf: to be defined, so they can be
     * used in the Turtle string without the @prefix definition.
     * <br /><br />
     * Example:
     * <br /><br />
     * <code>
     *     insert("isa:myfile rdf:type isa:file; isa:path '/path/to/my/file'");
     * </code>
     * @param data a Turtle String describing the resources and attributes.
     * @see <a href="https://www.w3.org/TR/turtle/">W3C Turtle Recommendation</a>
     */
    void insert(String data);

    /**
     * Remove statements with the given resource and property.
     * @param resourceURI the resources full uri
     * @param propertyURI the properties full uri
     */
    void removeStatements(String resourceURI, String propertyURI);
}
