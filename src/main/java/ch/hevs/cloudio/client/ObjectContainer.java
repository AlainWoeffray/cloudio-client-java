package ch.hevs.cloudio.client;

/**
 * Interface for all classes that can actually contain objects. Nodes and Objects can contain Objects.
 *
 * @see Node
 * @see ch.hevs.cloudio.client.Object
 */
public interface ObjectContainer extends UniqueIdentifiable {
    /**
     * Returns an existing or newly created object with the given name.
     *
     * If the object already exists, the actual object will be returned by this method, if no such object with the
     * given name exists, a new object is created, added to the node and returned by the method.
     *
     * @param objectName    Name (Identifier) of the object within the node.
     * @return              The Object instance.
     */
    ch.hevs.cloudio.client.Object object(String objectName);
}
