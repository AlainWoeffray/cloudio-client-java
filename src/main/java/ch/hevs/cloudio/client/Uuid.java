package ch.hevs.cloudio.client;

/**
 * Represents a unique ID. An object implementing the UniqueIdentifiable interface has to return an object implementing
 * the Uuid interface as return value of the method getUuid().
 *
 * The only mandatory operation such an Uuid has to offer is to allow it to be compared it with other UUIDs for
 * equality. However it is recommended that the standard Object's toString() method return a unique string as well, in
 * order to simplify development and trouble-shooting, but this is not actually required.
 *
 * @see UniqueIdentifiable
 */
public interface Uuid {
    /**
     * Returns true if the UUID is equal to the given one, false otherwise.
     *
     * @param other     The UUID to check equality with.
     * @return          True if equal, false otherwise.
     */
    boolean equals(Uuid other);
}
