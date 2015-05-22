package ch.hevs.cloudio.client;

/**
 * Represents an object which can be uniquely identified by an UUID. The actual implementation of the UUID object
 * returned by the get getUuid() is abstract as Uuid is just an interface.
 *
 * @see Uuid
 */
public interface UniqueIdentifiable extends NamedItem {
    /**
     * Returns a unique ID object that identifies the UniqueIdentifiable object.
     *
     * @return  The UUID object of the UniqueIdentifiable object.
     */
    Uuid getUuid();
}
