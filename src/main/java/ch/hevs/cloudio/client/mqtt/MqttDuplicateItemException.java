package ch.hevs.cloudio.client.mqtt;

class MqttDuplicateItemException extends Exception {
    MqttDuplicateItemException() {
        super("Items inside a NamedItemSet have to be unique!");
    }
}
