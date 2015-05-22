package ch.hevs.cloudio.client;

/**
 * The actual mode how changes on the Endpoint and all children of the Endpoint (Nodes, Objects and Attributes) are
 * published to the central cloud.iO platform.
 *
 * @see Endpoint
 */
public enum PublishMode {
    /**
     * This is the default mode for all new created Endpoints. This means that the client is not connected and any
     * change made to the structure or the actual data attributes will not trigger a message, nor a message will be
     * saved for later transmission to the cloud.iO platform. In order to actually connect to the cloud.iO backend,
     * you have to call the method Endpoint.setPublishMode() with another mode than OFFLINE in order to initiate a
     * connection to the cloud.iO cloud.
     */
    OFFLINE,

    /**
     * The changes to the structure, meta information and attributes are always send immediately. This is the simplest
     * method, as all changes are send without any other interaction with the Endpoint as just the update of the actual
     * value. Even if the client can not establish a connection to the cloud or the connection is temporary lost, the
     * messages are saved locally in order to send them later to the cloud.iO platform, so you do not have to care
     * about the fact if you are actually connected or not.
     */
    IMMEDIATE,

    /**
     * The changes to the structure, meta information and attributes are kept back until the commit() method of the
     * Endpoint instance is called. This mode enables atomic changes (multiple changes that will get send together) and
     * might reduce the actual network load, as multiple changes can be bundled into one single message. Even if the
     * client can not establish a connection to the cloud or the connection is temporary lost, the comitted messages are
     * saved locally in order to send them later to the cloud.iO platform, so you do not have to care about the fact if
     * you are actually connected or not.
     */
    COMMIT
}
