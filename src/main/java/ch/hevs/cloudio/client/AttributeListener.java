package ch.hevs.cloudio.client;

/**
 * All objects implementing this interface can be added as a listener in an Attribute in order to get notified when
 * the object has been changed from the cloud. It is important to understand that the listener does not get notified
 * if the attribute is changed locally, only changes from remote (cloud.iiO) will be published to all registered
 * listeners of an attribute in the local application.
 */
public interface AttributeListener {
    /**
     * The given attribute has been changed. The new value can be read from the attribute passed as argument to the
     * method. If you like to get informed about the change of the value before it is actually assigned to the
     * attribute, you can implement the AttributeValidator interface and register your class as validator within the
     * attribute.
     *
     * @param attribute The attribute that has been changed.
     * @see AttributeValidator
     */
    void attributeChanged(Attribute attribute);
}
