package ch.hevs.cloudio.client;

/**
 * Represents an object which can be uniquely identified by an UUID object. The kind of the UUID depends the actual
 * implementation of the actual implementation of the UniqueIdentifiable interface.
 */
public interface UniqueIdentifiable extends NamedItem {
    /**
     * Returns a unique ID object that identifies the UniqueIdentifiable object.
     *
     * @return  The UUID object of the UniqueIdentifiable object.
     */
    Uuid getUuid();
}
