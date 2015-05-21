package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.AttributeListener;
import ch.hevs.cloudio.client.UniqueIdentifiable;

import java.util.EmptyStackException;
import java.util.Stack;

interface MqttContainer extends AttributeListener, MqttSynchronizable {
    void containerChanged(MqttContainer container);
    UniqueIdentifiable locate(Stack<String> path) throws EmptyStackException;
}
