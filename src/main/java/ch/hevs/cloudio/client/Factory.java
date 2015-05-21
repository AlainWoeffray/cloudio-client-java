package ch.hevs.cloudio.client;

import java.util.Properties;

/**
 * The Factory will be used to create actual instances of Endpoints. @todo: A little more...
 *
 * @see Endpoint
 */
public class Factory {
    /**
     * A ThingFactory has to implement this method in order to return a valid Thing. Note that the factory class has to
     * be named <b>ThingFactory</b> in a package like this: <b>ch.heivs.iot.thing.{PROTOCOL}</b>. Otherwise the static
     * instantiation method will not find the factory!
     *
     * @param uuid          The UUID of the Thing to create.
     * @param properties    Properties for the Thing to be created.
     * @return               A new Thing initialized using the given properties.
     * @throws Exception    An exception is thrown in the case of an error. As there might be any exceptions thrown by
     *                      actual factories implementing this interface, the definition is broad.
     */
    public Endpoint createEndpoint(String uuid, Properties properties) throws Exception {
        return ((Factory)Factory.class.getClassLoader().loadClass("ch.hevs.cloudio.client.mqtt.Factory")
                .newInstance()).createEndpoint(uuid, properties);
    }
}
