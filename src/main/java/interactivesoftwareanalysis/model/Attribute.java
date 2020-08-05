package interactivesoftwareanalysis.model;

/**
 * An attribute of a resource in the model.
 * If a resource has multiple instances of a property, this only represents one of them.
 */
public interface Attribute {

    /**
     * Retrieve the Model associated with this attribute
     * @return This attributes model
     */
    Model getModel();

    /**
     * Retrieve the attributes value
     * @return the literal or uri of the attribute
     */
    String getValue();

    /**
     * Retrieve the resource this attribute belongs to
     * @return the associated resource object
     */
    Resource getResource();
}
