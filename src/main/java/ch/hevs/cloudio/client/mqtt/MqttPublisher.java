package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.UniqueIdentifiable;

interface MqttPublisher {
    void update(UniqueIdentifiable object);
}
