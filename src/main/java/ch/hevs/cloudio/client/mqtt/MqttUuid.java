package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.Uuid;

class MqttUuid implements Uuid {
    // In the case of the MQTT transport, the topic is the UUID for every object.
    private String topic;

    // Create the UUID for a MQTT Endpoint.
    public MqttUuid(MqttEndpoint endpoint) {
        topic = endpoint.getName();
    }

    // Create the UUID for a Node.
    public MqttUuid(MqttNode node) {
        topic = node.getParent().getUuid().toString() + "/nodes/" + node.getName();
    }

    // Create the UUID for an Object.
    public MqttUuid(MqttObject object) {
        topic = object.getParent().getUuid().toString() + "/objects/" + object.getName();
    }

    // Create the UUID for an Attribute.
    public MqttUuid(MqttAbstractAttribute attribute) {
        topic = attribute.getParent().getUuid().toString() + "/attributes/" + attribute.getName();
    }

    // Returns the topic.
    public String getTopic() {
        return topic;
    }


    /*** Uuid Implementation ******************************************************************************************/

    @Override
    public boolean equals(Uuid uuid) {
        // The other UUID must be of type MqttUuid and containing the same UUID in order to be equal.
        return uuid instanceof MqttUuid && topic.equals(((MqttUuid)uuid).getTopic());
    }

    @Override
    public String toString() {
        // Return the topic (which is unique).
        return topic;
    }
}
