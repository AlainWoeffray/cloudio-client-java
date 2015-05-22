package ch.hevs.cloudio.client;

/**
 * An AttributeValidator instance can be used to validate new values for attributes even before they are set on
 * attributes. Each Attribute can have exactly one validator which checks if the new value is valid. The validator is
 * used for local and remote setters. In the case a validator does not accept the value given using a local setter,
 * an exception is thrown. In the case of a remote setter the value is just ignored.
 */
public interface AttributeValidator<T> {
    /**
     * This method is called each time before actually changing a value of an attribute. If the method returns true, the
     * attribute is set to the new value, otherwise the new value is ignored.
     *
     * @param attribute The attribute that is subject of the value change.
     * @param newValue  The new value to apply.
     * @return True if the new value is valid, false otherwise.
     */
    boolean validate(Attribute attribute, T newValue);

    /**
     * Each AttributeValidator should return a string description (if possible a mathematical) what it actually
     * validates.
     *
     * @return String description of the validator.
     */
    String toString();
}
