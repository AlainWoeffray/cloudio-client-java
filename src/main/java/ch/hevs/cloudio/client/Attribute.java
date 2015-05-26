package ch.hevs.cloudio.client;

/**
 * Represents the atomic data elements all "Endpoints" are made of. An attribute basically has a constraint, a timestamp
 * when it has changed the last time and of course a value. Attributes are the leaves of all data models of Endpoints.
 *
 * The value can be any Java POJO or object that can be serialized into JSON (implements the Jackson JsonSerializable
 * interface.
 */
public interface Attribute<T> extends UniqueIdentifiable {
    /**
     * Returns the constraint of the attribute. The default constraint of a attribute is UNSPECIFIED. It is recommended
     * to change that before actually connecting the endpoint to the cloud.iO broker.
     *
     * @return  Attribute constraint.
     */
    AttributeConstraint getConstraint();

    /**
     * Changes the attributes constraint to the given value and returns a reference to that attribute in order to
     * chain methods. Note that event it is theoretically possible to change attributes constraints over time, we
     * do not encourage you to do so, as this might create issues within cloud.iO applications that use the endpoint's
     * data model. All constraints can be set with the exception of UNSPECIFIED, if you try to set the constraint to
     * AttributeConstraint.UNSPECIFIED an exception will be thrown.
     *
     * @param constraint                The Constraint to set.
     * @return                          Reference to the attribute itself in order to chain method calls.
     * @throws IllegalArgumentException Thrown if the enumeration value is not supported or not allowed.
     */
    Attribute constraint(AttributeConstraint constraint) throws IllegalArgumentException;

    /**
     * Returns the actual value of the attribute or null if the attribute does not has a value yet.
     *
     * @return  Value of the attribute or null.
     */
    T getValue();

    /**
     * Sets the value of the attribute to the given value. Note that the timestamp is updated with the current system
     * time if using this method signature. If the value can not be applied, an IllegalArgumentException will be thrown.
     * Note that this includes failures from validators, so if the configured AttributeValidator of the attribute
     * fails, an IllegalArgumentException is thrown too.
     *
     * Only attributes with constraints AttributeConstraint.STATUS and AttributeConstraint.MEASURE can be changed
     * anytime from the endpoint's context, when trying to change an attribute with another constraint, an
     * IllegalAccessError exception will be thrown. Attributes with all other constraints can be
     * changed exactly once and only in unconnected state.
     *
     * @param value                     The actual value to set.
     * @throws IllegalArgumentException This exception is thrown if the value is not suitable for an attribute.
     * @throws IllegalAccessError       Thrown if the constraint is different as STATUS and MEASURE or STATIC when
     *                                  while setting initial value.
     */
    void setValue(T value) throws IllegalAccessError;

    /**
     * Sets the value of the attribute to the given value. Note that the timestamp is updated with the given one. If
     * the value can not be applied, an IllegalArgumentException will be thrown. Note that this includes failures from
     * validators, so if the configured AttributeValidator of the attribute fails, an IllegalArgumentException is
     * thrown too.
     *
     * Only attributes with constraints, AttributeConstraint.STATUS and AttributeConstraint.MEASURE can be changed
     * anytime from the endpoint's context, when trying to change an attribute with another constraint, an
     * IllegalAccessError exception will be thrown.
     *
     * @param value                         The actual value of to set.
     * @param timestamp                     Timestamp in seconds since epoch (floating point).
     * @throws IllegalArgumentException     This exception is thrown if the value is not suitable for an attribute.
     * @throws IllegalAccessError           Thrown if the constraint is different as STATUS and MEASURE or STATIC when
     *                                      while setting initial value.
     */
    void setValue(T value, float timestamp) throws IllegalAccessError;

    /**
     * Initializes the attribute with the given value. If a validator was already configured, it will be applied
     * before the value is set for the attribute. Note that this method can only be called when the
     * endpoint is not already online and the attribute value is empty.
     *
     * @param initialValue                  The initial value for the attribute.
     * @param timestamp                     The timestamp in seconds since epoch.
     * @return                              Reference to the attribute in order to chain method calls.
     * @throws IllegalArgumentException     Thrown if the given value is not applicable or invalid.
     * @throws IllegalStateException        Thrown if the endpoint is online or the attribute already has a value.
     */
    Attribute initialize(T initialValue, float timestamp)
            throws IllegalArgumentException, IllegalStateException;

    /**
     * Returns the timestamp of the current attribute value as seconds since UNIX Epoch. Resolutions smaller than
     * a second can be achieved as the datatype is a floating point number.
     *
     * @return  Timestamp of the attribute value in seconds since Unix Epoch.
     */
    float getCurrentValueTimestamp();

    /**
     * Sets the validator to use for the given attribute. Default is null which results in no validity checks at all
     * when setting the attribute.
     *
     * The attribute validator will be used for both attribution mutation scenarios: A local call of the setValue()
     * methods or a change from a remote controlling application.
     *
     * If there was already a validator installed before, it will be replaced by the new one. If you need more than
     * one validator at a time for the same attribute, check out the CompositeValidator class.
     *
     * @param validator The validator to use in order to validate the value after applying it to the attribute.
     * @return          Reference to the attribute itself in order to enable method call chaining.
     * @see AttributeValidator
     * @see ch.hevs.cloudio.client.validator.CompositeValidator
     */
    Attribute validator(AttributeValidator<? extends Comparable<T>> validator);

    /**
     * Adds an attribute listener. It is important to know that the listener gets only called of a value has been
     * changed from the network, local changes do not trigger a notification to the listeners.
     *
     * @param listener  Listener to add.
     */
    void addAttributeListener(AttributeListener listener);

    /**
     * Removes an attribute listener.
     *
     * @param listener  Listener to remove.
     */
    void removeAttributeListener(AttributeListener listener);
}
