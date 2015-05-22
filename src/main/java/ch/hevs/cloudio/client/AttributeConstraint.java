package ch.hevs.cloudio.client;

/**
 * Defines the different possible attribute constraints.
 */
public enum AttributeConstraint {
    /**
     * No constraint was specified at all. This value should be avoided by any means.
     */
    UNSPECIFIED,

    /**
     * The attribute is a static value and can't be changed during runtime.
     */
    STATIC,

    /**
     * The attribute is a parameter that can be configured from the cloud and which's value should be saved locally on
     * the "Endpoint". Note that the cloud.iO communication library will not save the value, it is the responsibility
     * of you to actually save the configuration to a persistent location.
     */
    PARAMETER,

    /**
     * The attribute is a status.
     */
    STATUS,

    /**
     * The attribute is a set point that can be changed from the cloud. Note that there is no guarantee that the value
     * of set points are stored within the "Endpoint" and might be initialized to the default value on the next power
     * cycle.
     */
    SET_POINT,

    /**
     * The attribute is a measure of any kind and can change at any time.
     */
    MEASURE
}
