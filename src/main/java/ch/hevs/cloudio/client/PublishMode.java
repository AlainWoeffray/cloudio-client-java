package ch.hevs.cloudio.client;

/**
 * The actual mode how changes on the Thing and all sub-objects (Node, Object and Attributes) are published to the
 * central IOT platform.
 *
 * @see Endpoint
 */
public enum PublishMode {
    /**
     * This is the default mode for all new created Things. This means that the IoT system is not connected at all and
     * every change made to the structure or the actual data attributes will not have any effect. In order to actually
     * connect to the IoT backend, you have to call the method Thing.setPublishMode() with another mode than the
     * OFFLINE mode in order to connect to the IoT cloud.
     */
    OFFLINE,

    /**
     * The changes to the structure, meta information and attributes are send immediately. This is the simplest method,
     * as all changes are send without any other interaction with the Thing, but might lead to more traffic and atomic
     * changes of multiple attributes are not possible in this mode.
     */
    IMMEDIATE,

    /**
     * The changes to the structure, meta information and attributes are kept back until the commit() method of the
     * Thing instance is called. This mode enables atomic changes (multiple changes that will get send together) and
     * might reduce the actual network load, as multiple changes can be bundled into one single message.
     */
    COMMIT
}
