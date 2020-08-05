package interactivesoftwareanalysis.model;

import java.util.List;

/**
 * A resource in a {@link Model}
 */
public interface Resource {

    /**
     * Retrieve all attributes of this resource
     * @return a list of this resources attributes or an empty list, if it does not have any.
     */
    List<Attribute> getAttributes();

    /**
     * Retrieve all attributes of this resource, that have the given
     * attribute type (or 'property name')
     * @param attributeTypeUri the attribute types uri to look for
     * @return a list of matching attributes or an empty list, if no attributes match
     */
    List<Attribute> getAttributes(String attributeTypeUri);

    /**
     * Add a literal attribute to this resource
     * @param attributeTypeUri the uri of the attribute type (or 'property name') to add
     * @param value the literal value of the attribute to add
     */
    void addAttribute(String attributeTypeUri, String value);

    /**
     * Add a resource attribute to this resource
     * @param attributeTypeUri the uri of the attribute type (or 'property name') to add
     * @param valueUri the uri of the resource value to add
     */
    void addResourceAttribute(String attributeTypeUri, String valueUri);

    /**
     * Retrieve all tags associated with this resource
     * @return a list of tags or an empty list, if the resource does not have any.
     */
    List<Tag> getTags();

    /**
     * Add a tag to this resource
     * @param tagName the tag name for the new tag
     * @param tagDetail a description or reason for adding this tag
     */
    void addTag(String tagName, String tagDetail);

    /**
     * Retrieve this resources uniform resource identifier
     * @return the uri including namespace
     */
    String getUri();

    /**
     * Retrieve the model this resource is associated with
     * @return this resources model
     */
    Model getModel();
}
