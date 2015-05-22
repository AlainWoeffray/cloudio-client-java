package ch.hevs.cloudio.client;

/**
 * Represents a logical node, this can be an actual device or a function of a device. Nodes enable to separate multiple
 * sensors that are connected using a gateway (which acts as the "Endpoint") or a node can represent a certain
 * functionality within an Endpoint allowing to group functions together.
 *
 * A node can implement multiple interfaces. Interfaces are not like in Java definitions of methods to implement,
 * interfaces of a node rather describes what structure the node has to offer including objects and attributes.
 */
public interface Node extends ObjectContainer {
    /**
     * Returns true if the node implements the interface identified by the given string.
     *
     * @param interfaceName String identifying the interface.
     * @return              True if the node implements the interface, false otherwise.
     */
    boolean isImplementing(String interfaceName);

    /**
     * Adds the given interface identification strings to the list of implemented interfaces of the node.
     *
     * @param implementedInterfaces List of strings implemented by the node.
     * @return                      Reference to the Node in order to chain method calls.
     */
    Node implement(String... implementedInterfaces);
}
