package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

class MqttEndpoint implements Endpoint, MqttContainer, MqttPublisher, MqttCallback, JsonSerializable {
    private String uuid;
    private MqttNamedItemSet<MqttNode> nodes = new MqttNamedItemSet<MqttNode>();
    private MqttClient mqtt = null;
    private MqttConnectOptions options;
    private Properties properties;
    private PublishMode publishMode = PublishMode.OFFLINE;

    public MqttEndpoint(String uuid, MqttClient mqtt, MqttConnectOptions options, Properties properties) {
        this.uuid = uuid;
        this.mqtt = mqtt;
        this.options = options;
        this.properties = properties;
    }

    public String getJson() throws JsonProcessingException {
        return getJson(this);
    }

    private static String getJson(java.lang.Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }

    private void publishContent(String topic, java.lang.Object object) {
        if (publishMode != PublishMode.OFFLINE) {
            try {
                mqtt.publish(topic, getJson(object).getBytes("UTF-8"), 1, true);
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() throws IOException, MqttException {
        if (mqtt == null || mqtt.isConnected()) return;

        mqtt.connect(options);
        mqtt.setCallback(this);
        mqtt.publish("@online/" + uuid, getJson().getBytes("UTF-8"), 1, true);
        mqtt.subscribe("@set/" + uuid + "/#");
        setSynchronized();
    }

    public void disconnect() {
        if (mqtt == null) return;
        try {
            mqtt.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /*** Endpoint Implementation **************************************************************************************/

    @Override
    public String getName() {
        return uuid;
    }

    @Override
    public Uuid getUuid() {
        return new MqttUuid(this);
    }

    @Override
    public Node node(String nodeName) {
        MqttNode node = nodes.getItem(nodeName);
        if (node == null) {
            try {
                node = new MqttNode(nodeName);
                nodes.addItem(node);
                node.setParent(this);
                containerChanged(this);
            } catch (MqttDuplicateItemException e) {
                node = nodes.getItem(nodeName);
            }
        }
        return node;
    }

    @Override
    public void setPublishMode(PublishMode publishMode) throws Exception {
        if (publishMode == PublishMode.OFFLINE) {
            disconnect();
        } else {
            connect();
        }
        this.publishMode = publishMode;
    }

    @Override
    public void commit() {
        if (publishMode == PublishMode.COMMIT) {
            commit(this);
        }
    }


    /*** MqttContainer Implementation *********************************************************************************/

    @Override
    public void attributeChanged(Attribute attribute) {
        if (publishMode == PublishMode.IMMEDIATE) {
            update(attribute);
            if (attribute instanceof MqttIntegerAttribute) {
                ((MqttIntegerAttribute)attribute).setSynchronized();
            }
        }
    }

    @Override
    public void containerChanged(MqttContainer container) {
        if (publishMode == PublishMode.IMMEDIATE) {
            update(container);
            container.setSynchronized();
        }
    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public int getImmediateChildrenWithChangesCount() {
        int changes = 0;
        for (MqttNode node: nodes) {
            if (node.hasChanges()) {
                ++changes;
            }
        }

        return changes;
    }

    @Override
    public void setSynchronized() {
        for (MqttNode node: nodes) {
            node.setSynchronized();
        }
    }

    @Override
    public void commit(MqttPublisher publisher) {
        if (getImmediateChildrenWithChangesCount() > 1) {
            publisher.update(this);
            setSynchronized();
        } else {
            for (MqttNode node: nodes) {
                node.commit(publisher);
            }
        }
    }

    @Override
    public UniqueIdentifiable locate(Stack<String> path) throws EmptyStackException {
        if (path.isEmpty()) {
            return this;
        } else if ("nodes".equals(path.pop())) {
            MqttNode node = nodes.getItem(path.pop());
            if (node != null) {
                return node.locate(path);
            }
        }

        return null;
    }

    /*** MqttPublisher Implementation *********************************************************************************/

    public void update(UniqueIdentifiable object) {
        publishContent("@update/" + object.getUuid(), object);
    }


    /*** JsonSerializable Implementation ******************************************************************************/

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        if (!nodes.isEmtpy()) {
            gen.writeObjectField("nodes", nodes);
        }
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {}


    /*** MqttCallback Implementation **********************************************************************************/

    @Override
    public void connectionLost(Throwable throwable) {
        // TODO:...
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String[] splitTopic = topic.split("/");
        Stack<String> location = new Stack<String>();
        for (int i = splitTopic.length - 1; i >= 0; --i) {
            location.push(splitTopic[i]);
        }

        try {
            // The topic has to start with "@set/<UUID>".
            if ("@set".equals(location.pop())) {
                if (uuid.equals(location.pop())) {
                // Get the object identified by the topic.
                final UniqueIdentifiable attribute = locate(location);

                // Only operations on single attributes are allowed in the current version!
                if (attribute instanceof MqttIntegerAttribute) {
                    ObjectMapper mapper = new ObjectMapper();
                    final Integer value = mapper.readValue(message.getPayload(), Integer.class);

                    // Workaround for bug (probably in RabbitMQ) where we can not publish a new message before acking
                    // the reception of this message. TODO: Check if the bug is in PAHO or RabbitMQ MQTT plugin.
                    (new Timer()).schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ((MqttIntegerAttribute)attribute).setValueFromMqtt(value);
                        }
                    }, 0);
                }}
            }
        } catch (Exception e) {
            // We silently ignore the message if the topic is invalid.
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // No action necessary.
    }
}
