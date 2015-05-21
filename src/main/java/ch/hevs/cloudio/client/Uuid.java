package ch.hevs.cloudio.client;

/**
 * Represents a unique ID.
 *
 * The only mandatory operation of such an Uuid has to offer is to allow it to be compared it with other UUIDs for
 * equality. However it is recommended that the standard Object's toString() method return a unique string as well,
 * but this is not required actually.
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
