package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.Attribute;
import ch.hevs.cloudio.client.Node;
import ch.hevs.cloudio.client.UniqueIdentifiable;
import ch.hevs.cloudio.client.Uuid;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.*;

class MqttNode implements Node, MqttObjectContainer, JsonSerializable {

    private MqttEndpoint parent = null;
    private String name;

    private Set<String> implementedInterfaces = new TreeSet<String>();

    private MqttNamedItemSet<MqttObject> objects = new MqttNamedItemSet<MqttObject>();

    private boolean outOfSync = true;

    public MqttNode(String name, String... implementedInterfaces) {
        this.name = name;
        this.implementedInterfaces.addAll(Arrays.asList(implementedInterfaces));
    }

    MqttEndpoint getParent() {
        return parent;
    }

    void setParent(MqttEndpoint parent) {
        this.parent = parent;
    }


    /*** Node Implementation ******************************************************************************************/

    @Override
    public String getName() {
        return name;
    }

    private Uuid uuid = null;
    @Override
    public Uuid getUuid() {
        // Lazy initialisation of UUID.
        if (uuid == null) {
            uuid = new MqttUuid(this);
        }
        return new MqttUuid(this);
    }

    @Override
    public boolean isImplementing(String implInterface) {
        return implementedInterfaces.contains(implInterface);
    }

    @Override
    public Node implement(String... implementedInterfaces) {
        // Count the number of implemented interfaces (only if the count changes we are out of sync).
        int count = this.implementedInterfaces.size();

        // Add all new interfaces.
        this.implementedInterfaces.addAll(Arrays.asList(implementedInterfaces));

        // Has the number of implemented interfaces changed?
        if (count  != this.implementedInterfaces.size()) {
            outOfSync = true;
            parent.containerChanged(this);
        }

        return this;
    }

    @Override
    public ch.hevs.cloudio.client.Object object(String objectName) {
        // Try to get the child object with the given name.
        MqttObject object = objects.getItem(objectName);

        if (object == null) {
            try {
                // If it does not exist, create a new object with the given name and add it to the set.
                object = new MqttObject(objectName);
                objects.addItem(object);

                // Set this instance as the parent of the object.
                object.setParent(this);

                // Notify my parent that this container has changes.
                if (parent != null) {
                    parent.containerChanged(this);
                }
            } catch (MqttDuplicateItemException e) {
                // Should never happen, but if the object exists now?? the best we can do is to use it.
                object = objects.getItem(objectName);
            }
        }

        // Return the existing or newly created object.
        return object;
    }


    /*** MqttObjectContainer Implementation ***************************************************************************/

    @Override
    public void attributeChanged(Attribute attribute) {
        // Relay the event to the parent.
        parent.attributeChanged(attribute);
    }

    @Override
    public void containerChanged(MqttContainer container) {
        // Relay the event to the parent.
        parent.containerChanged(container);
    }

    @Override
    public boolean hasChanges() {
        // Does the node has changes?
        return outOfSync;
    }

    @Override
    public int getImmediateChildrenWithChangesCount() {
        int changes = 0;

        // Count the number of objects with changes.
        for (MqttObject object: objects) {
            if (object.hasChanges() || object.doChildrenHaveChanges()) {
                ++changes;
            }
        }

        return changes;
    }

    @Override
    public void setSynchronized() {
        // Set the object himself as in sync.
        outOfSync = false;

        // Set all objects to be in sync.
        for (MqttObject object: objects) {
            object.setSynchronized();
        }
    }

    @Override
    public void commit(MqttPublisher publisher) {
        // If the node has changes or more than 1 objects have changes, publish the whole node.
        if (hasChanges() || getImmediateChildrenWithChangesCount() > 1) {
            publisher.update(this);
            setSynchronized();
        } else {
            // Call the commit method for all objects.
            for (MqttObject object: objects) {
                object.commit(publisher);
            }
        }
    }

    @Override
    public UniqueIdentifiable locate(Stack<String> path) throws EmptyStackException {
        // If the path is empty now, it refers to the node itself.
        if (path.isEmpty()) {
            return this;
        } else {
            // We only can locate objects at this hierarchy level.
            if ("objects".equals(path.pop())) {
                MqttObject object = objects.getItem(path.pop());

                // If the object exists continue, otherwise return null.
                if (object != null) {
                    return object.locate(path);
                }
            }
        }

        return null;
    }

    /*** JsonSerializable Implementation ******************************************************************************/
    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Start object.
        gen.writeStartObject();

        // Include all implemented interfaces.
        if (!implementedInterfaces.isEmpty()) {
            gen.writeObjectField("implements", implementedInterfaces);
        }

        // Write all objects.
        if (!objects.isEmtpy()) {
            gen.writeObjectField("objects", objects);
        }

        // End object.
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {}
}
