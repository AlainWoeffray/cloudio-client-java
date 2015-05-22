package ch.hevs.cloudio.client;

/**
 * Represents an object. This is nothing other than a structured data container. An object can contain child objects
 * and attributes. Attributes are a Java Properties equivalent with some additional meta information.
 *
 * An object can declare conformity to a given data class. A data class basically defines which exact child objects and
 * attributes structure the object has to be made of.
 */
public interface Object extends ObjectContainer {
    /**
     * Returns true if the object is conform to the data class identified by the given string.
     *
     * @param dataClass     String describing the actual data class.
     * @return              True if the object conforms to the given class, false otherwise.
     */
    boolean isConform(String dataClass);

    /**
     * Sets the object's conformity to the given data class description string. Note that if you try to declare a
     * conformity of an object that already has a conformity setup, an exception of type IllegalStateException will
     * be thrown.
     *
     * @param conforms                  Data class as string.
     * @return                          Reference to itself in order to chain methods.
     * @throws IllegalStateException    Is thrown if the object already is conform to a data class.
     */
    ch.hevs.cloudio.client.Object conforms(String conforms) throws IllegalStateException;

    /**
     * Returns an existing or newly created attribute with the given name.
     *
     * If the attribute already exists, the actual attribute will be returned by this method, if no such attribute
     * with the given name exists, a new attribute is created, added to the object and returned by the method.
     *
     * @param attributeName Name (Identifier) of the attribute within the object.
     * @param attributeType The actual type of the attribute.
     * @return              The Attribute instance.
     */
    <T> Attribute<T> attribute(String attributeName, Class<T> attributeType);

    /**
     * Adds a static attribute with the given name and the given value and returns the reference to itself. Basically
     * this is only a shortcut for:
     *
     * <pre>
     *      String name = ...;
     *      Object value = ...;
     *      object.attribute(name).constraint(AttributeConstraint.STATIC).setValue(value);
     * </pre>
     *
     *
     * @param name  Attribute name.
     * @param value The value of the static attribute. Can not be changed afterwards.
     * @return      Reference to the object itself in order to chain method calls.
     */
    <T> ch.hevs.cloudio.client.Object staticAttribute(String name, T value) throws IllegalArgumentException;
}
