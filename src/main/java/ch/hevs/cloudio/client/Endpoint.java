package ch.hevs.cloudio.client;

/**
 * Represents a communication endpoint. This can be a device which supports the cloud.iO data model directly, or it can
 * be an adapter (gateway) for one or more actual devices. The functionality of a device is always implemented using
 * nodes, so in the case the "thing" communicates with the IoT cloud natively it would normally only have one actual
 * node, in the case of a gateway, a node could be created for each actual device (thing) communicating with the
 * gateway.
 */
public interface Endpoint extends UniqueIdentifiable {
    /**
     * Returns an existing or newly created node with the given name.
     *
     * If the node already exists, the actual node will be returned by this method, if no such node with the given name
     * exists, a new node is created, added to the thing and returned by the method.
     *
     * @param nodeName  Name (Identifier) of the node within the thing.
     * @return          The node instance.
     */
    Node node(String nodeName);

    /**
     * Changes the publishing mode. The default mode is ThingPublishMode.OFFLINE. This means that every Thing starts
     * not connected and as soon as the mode will be changed, the Thing tries to connect to the cloud backend.
     *
     * @param mode                  The publish mode to set.
     * @throws Exception  Broad exception definition as the actual implementation can throw any exception.
     * @see PublishMode
     */
    void setPublishMode(PublishMode mode) throws Exception;

    /**
     * If the Thing is in ThingPublishMode.COMMIT, the call to this method will trigger the transmission of the actual
     * state of all attributes and structural changes since the last call to commit. If the Thing object is in another
     * mode, no action is taken at all.
     *
     * @see PublishMode
     */
    void commit();
}