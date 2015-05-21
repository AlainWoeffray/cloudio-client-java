package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.UniqueIdentifiable;

interface MqttSynchronizable extends UniqueIdentifiable {
    boolean hasChanges();
    int getImmediateChildrenWithChangesCount();

    void setSynchronized();
    void commit(MqttPublisher publisher);
}
