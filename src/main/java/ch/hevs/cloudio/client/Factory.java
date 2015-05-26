package ch.hevs.cloudio.client;

import ch.hevs.cloudio.client.mqtt.MqttFactory;

import java.util.Properties;

/**
 * The Factory will be used to create actual instances of Endpoints. @todo: A little more...
 *
 * @see Endpoint
 */
public class Factory {
    /**
     * The Factory can be used to instantiate valid Endpoints.
     *
     * @param uuid          The UUID of the Endpoint to create.
     * @param properties    Properties for the Endpoint to be created.
     * @return               A new Endpoint initialized using the given properties.
     * @throws Exception    An exception is thrown in the case of an error. As there might be any exceptions thrown by
     *                      actual factories implementing this interface, the definition is broad.
     */
    public Endpoint createEndpoint(String uuid, Properties properties) throws Exception {
        return (new MqttFactory()).createEndpoint(uuid, properties);
    }
}
