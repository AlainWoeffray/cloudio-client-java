package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.Endpoint;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Properties;

// TODO: Move from Jackson databind to Jackson Stream implementation.

public class Factory extends ch.hevs.cloudio.client.Factory {

    // MQTT options.
    private static final String MQTT_HOST_URI_PROPERTY          = "ch.hevs.cloudio.client.hostUri";
    private static final String MQTT_CONNECTION_TIMEOUT_PROPERTY= "ch.hevs.cloudio.client.timeout";
    private static final String MQTT_KEEPALIVE_INTERVAL_PROPERTY= "ch.hevs.cloudio.client.keepAliveInterval";
    private static final String MQTT_PERSISTENCE_MEMORY         = "memory";
    private static final String MQTT_PERSISTENCE_FILE           = "file";
    private static final String MQTT_PERSISTENCE_PROPERTY       = "ch.hevs.cloudio.client.persistence";
    private static final String MQTT_PERSISTENCE_DEFAULT        = MQTT_PERSISTENCE_MEMORY;

    // SSL options.
    private static final String ENDPOINT_IDENTITY_FILE_TYPE = "PKCS12";
    private static final String ENDPOINT_IDENTITY_MANAGER_TYPE = "SunX509";
    private static final String ENDPOINT_IDENTITY_FILE_PROPERTY = "ch.hevs.cloudio.client.ssl.clientCert";
    private static final String ENDPOINT_IDENTITY_PASS_PROPERTY = "ch.hevs.cloudio.client.ssl.clientPassword";
    private static final String ENDPOINT_IDENTITY_PASS_DEFAULT = "";
    private static final String CERT_AUTHORITY_FILE_TYPE        = "JKS";
    private static final String CERT_AUTHORITY_MANAGER_TYPE     = "SunX509";
    private static final String CERT_AUTHORITY_FILE_PROPERTY    = "ch.hevs.cloudio.client.ssl.authorityCert";
    private static final String CERT_AUTHORITY_FILE_DEFAULT     = "classpath:authority.jks";
    private static final String CERT_AUTHORITY_PASS_PROPERTY    = "ch.hevs.cloudio.client.ssl.authorityPassword";
    private static final String CERT_AUTHORITY_PASS_DEFAULT     = "";
    private static final String SSL_PROTOCOL_PROPERTY           = "ch.hevs.cloudio.client.ssl.protocol";
    private static final String SSL_PROTOCOL_DEFAULT            = "TLSv1.2";

    /**
     *
     * <p><i>Supported properties are:</i></p>
     *
     * <ul>
     *     <li><b>ch.hevs.cloudio.client.hostUri</b><br>
     *     The URI of the MQTT broker to connect to (Ex. 'ssl://myhost.net:8883').
     *     The property is <b>mandatory</b>.<br><br></li>
     *     <li><b>ch.hevs.cloudio.client.timeout</b><br>
     *     The connection timeout in seconds. If not provided, the default MQTT timeout (30 seconds) is used.
     *     <br><br></li>
     *     <li><b>ch.hevs.cloudio.client.keepAliveInterval</b><br>
     *     The connection keep alive interval in seconds. If not provided, the default MQTT keep alive interval
     *     (60 seconds) is used.<br><br></li>
     *     <li><b>ch.hevs.cloudio.client.persistence</b><br>
     *     The persistence to use for the MQTT client. Possible values are <b>"memory"</b> where the data is saved in
     *     memory or <b>"file"</b> where the messages are queued on the filesystem. Default is <b>"memory"</b>
     *     <br><br></li>
     *     <li><b>ch.hevs.cloudio.client.ssl.clientCert</b><br>
     *     Location (URI) of the ssl client certificate and key bundled into a PKCS12 file (*.p12). Supported URI
     *     schemes are <b>file:</b>, which searches the identity file on the local filesystem or <b>classpath:</b>
     *     whereas the certificate is searched in the classpath (jar, fat-jar). If this property is not present,
     *     the SSL key and certificate are loaded from <b>classpath:{UUID}.p12</b>.<br><br></li>
     *     <li><b>ch.hevs.cloudio.client.ssl.clientPassword</b><br>
     *     Password in order to open the PKCS12 identity file for the SSL client. If the property is not present,
     *     no password is used at all.<br><br></li>
     *     <li><b>ch.hevs.cloudio.client.ssl.authorityCert</b><br>
     *     Location (URI) of the certificate authorities certificate (JKS format). Supported URI schemes are
     *     <b>file:</b>, which searches the identity file on the local filesystem or <b>classpath:</b> whereas the
     *     certificate is searched in the classpath (jar, fat-jar). If this property is not present, the SSL key and
     *     certificate are loaded from <b>classpath:authority.jks</b>.<br><br></li>
     *     <li><b>ch.hevs.cloudio.client.ssl.authorityPassword</b><br>
     *     Password in order to open the JKS identity file for the SSL client. If the property is not present,
     *     no password is used at all.<br><br></li>
     *     <li><b>ch.hevs.cloudio.client.ssl.protocol</b><br>
     *     SSL version to use, can be any of: <i>"SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2"</i>. We
     *     discourage you to use any SSL implementation and TLSv1, as they are insecure at the time of this writing.
     *     The default value if no version is specified will be <b>"TLSv1.2"</b>.</li>
     * </ul>
     *
     *
     * @param uuid          The UUID of the Endpoint.
     * @param properties    Properties for the Endpoint to create. If null, the device will try to load the properties
     *                      from the URI "classpath:{UUID}.properties" in the classpath. If no properties are given or
     *                      found, an IOException will be thrown.
     * @return              Returns an Endpoint backed by a secure MQTT communication with the cloud.
     * @throws Exception    If anything goes wrong during the initialization of the Endpoint object, an appropriate
     *                      exception will be thrown.
     */
    @Override
    public Endpoint createEndpoint(String uuid, Properties properties) throws Exception {

        // The ID has to be a valid string!
        if (uuid == null) {
            throw new IllegalArgumentException();
        }

        // Do we have to load the properties from the classpath ourselves?
        if (properties == null) {
            properties = new Properties();
            properties.load(getResource("classpath:" + uuid + ".properties"));
        }

        // Create a SSL based MQTT option object.
        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(createSocketFactory(uuid, properties));
        options.setCleanSession(true);
        if (properties.contains(MQTT_CONNECTION_TIMEOUT_PROPERTY)) {
            options.setConnectionTimeout(Integer.parseInt(properties.getProperty(MQTT_CONNECTION_TIMEOUT_PROPERTY)));
        }
        if (properties.contains(MQTT_KEEPALIVE_INTERVAL_PROPERTY)) {
            options.setKeepAliveInterval(Integer.parseInt(properties.getProperty(MQTT_KEEPALIVE_INTERVAL_PROPERTY)));
        }
        options.setWill("@offline/" + uuid, new byte[0], 1, false);

        // Create persistence object.
        MqttClientPersistence persistence = null;
        String persistenceProvider = properties.getProperty(MQTT_PERSISTENCE_PROPERTY, MQTT_PERSISTENCE_DEFAULT);
        if (persistenceProvider.equals(MQTT_PERSISTENCE_MEMORY)) {
            persistence = new MemoryPersistence();
        } else if (persistenceProvider.equals(MQTT_PERSISTENCE_FILE)) {
            persistence = new MqttDefaultFilePersistence();
        }

        // Create MQTT client.
        MqttClient mqtt = new MqttClient(properties.getProperty(MQTT_HOST_URI_PROPERTY), uuid, persistence);
        return new MqttEndpoint(uuid, mqtt, options, properties);
    }

    private SSLSocketFactory createSocketFactory(String endpointUuid, Properties properties) throws Exception {
        // Endpoint identity (Key & Certificate) in single PKCS #12 archive file named with the actual Endpoint ID.
        KeyStore endpointKeyCertStore = KeyStore.getInstance(ENDPOINT_IDENTITY_FILE_TYPE);
        endpointKeyCertStore.load(getResource(properties.getProperty(ENDPOINT_IDENTITY_FILE_PROPERTY,
                        "classpath:" + endpointUuid + ".p12")),
                properties.getProperty(ENDPOINT_IDENTITY_PASS_PROPERTY, ENDPOINT_IDENTITY_PASS_DEFAULT).toCharArray());
        KeyManagerFactory clientKeyCertManagerFactory = KeyManagerFactory.getInstance(ENDPOINT_IDENTITY_MANAGER_TYPE);
        clientKeyCertManagerFactory.init(endpointKeyCertStore, "".toCharArray());

        // Authority certificate in JKS format.
        KeyStore authorityKeyStore = KeyStore.getInstance(CERT_AUTHORITY_FILE_TYPE);
        authorityKeyStore.load(getResource(properties.getProperty(CERT_AUTHORITY_FILE_PROPERTY,
                        CERT_AUTHORITY_FILE_DEFAULT)),
                properties.getProperty(CERT_AUTHORITY_PASS_PROPERTY, CERT_AUTHORITY_PASS_DEFAULT).toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(CERT_AUTHORITY_MANAGER_TYPE);
        trustManagerFactory.init(authorityKeyStore);

        // Create SSL Context.
        SSLContext sslContext = SSLContext.getInstance(properties.getProperty(SSL_PROTOCOL_PROPERTY,
                SSL_PROTOCOL_DEFAULT));
        sslContext.init(clientKeyCertManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    private InputStream getResource(String location) throws FileNotFoundException, URISyntaxException {
        URI uri = new URI(location);
        if (uri.getScheme().equals("classpath")) {
            return getClass().getClassLoader().getResourceAsStream(uri.getSchemeSpecificPart());
        } else {
            Path path = Paths.get(uri);
            File file = path.toFile();
            return new FileInputStream(file);
        }
    }
}
