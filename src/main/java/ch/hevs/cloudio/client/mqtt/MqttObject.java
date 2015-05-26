package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.Attribute;
import ch.hevs.cloudio.client.AttributeConstraint;
import ch.hevs.cloudio.client.UniqueIdentifiable;
import ch.hevs.cloudio.client.Uuid;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Stack;

class MqttObject implements ch.hevs.cloudio.client.Object, MqttObjectContainer, JsonSerializable {

    private MqttObjectContainer parent = null;
    private String name;

    private String conforms = null;

    private MqttNamedItemSet<MqttObject> objects = new MqttNamedItemSet<MqttObject>();
    private MqttNamedItemSet<MqttAbstractAttribute> attributes = new MqttNamedItemSet<MqttAbstractAttribute>();

    private boolean outOfSync = true;

    protected MqttObject(String name) {
        this.name = name;
    }

    MqttObjectContainer getParent() {
        return parent;
    }

    void setParent(MqttObjectContainer parent) {
        this.parent = parent;
    }

    public boolean doChildrenHaveChanges() {
        for (MqttAbstractAttribute attribute: attributes) {
            if (attribute.hasChanges()) {
                return true;
            }
        }
        for (MqttObject object: objects) {
            if (object.hasChanges() || object.doChildrenHaveChanges()) {
                return true;
            }
        }

        return false;
    }


    /*** Object Implementation ****************************************************************************************/

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
    public boolean isConform(String dataClass) {
        return conforms != null && conforms.equals(dataClass);
    }

    @Override
    public ch.hevs.cloudio.client.Object conforms(String conforms) throws IllegalStateException {
        // Conforms can be called only once (conforms == null).
        if (this.conforms == null && conforms != null) {
            // Change conformity.
            this.conforms = conforms;

            // Update.
            outOfSync = true;
            parent.containerChanged(this);
        } else {
            throw new IllegalStateException("Can not declare conformity '" + conforms + "'Object already conform to '" +
                    this.conforms + "'");
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

    @Override
    public Attribute attribute(String attributeName, Class attributeType) {
        // Try to get the child attribute with the given name.
       MqttAbstractAttribute attribute = attributes.getItem(attributeName);
        if (attribute == null) {
            try {
                // If it does not exist, create a new attribute with the given name and add it to the set.
                if (attributeType == Integer.class) {
                    attribute = new MqttIntegerAttribute(attributeName);
                } else if (attributeType == Boolean.class) {
                    attribute = new MqttBooleanAttribute(attributeName);
                } else if (attributeType == Float.class || attributeType == Double.class) { // todo: Add double data type.
                    attribute = new MqttNumberAttribute(attributeName);
                } else if (attributeType == String.class) {
                    attribute = new MqttStringAttribute(attributeName);
                } else {
                    throw new IllegalArgumentException();
                }
                attributes.addItem(attribute);

                // Set this instance as the parent of the attribute.
                attribute.setParent(this);

                // Notify my parent that this container has changes.
                parent.containerChanged(this);

            } catch (MqttDuplicateItemException e) {
                // Should never happen, but if the attribute exists now?? the best we can do is to use it.
                attribute = attributes.getItem(attributeName);
            }
        }

        // Return the existing or newly created attribute.
        return attribute;
    }

    @Override
    public <T> ch.hevs.cloudio.client.Object staticAttribute(String attributeName, T value)
            throws IllegalArgumentException {
        try {
            // Add the static attribute. The exceptions should be NEVER thrown and if so, silently ignore them.
                attribute(attributeName, value.getClass()).constraint(AttributeConstraint.STATIC).initialize(value, 0);
        } catch (IllegalStateException e) {
        } catch (IllegalAccessError e) {}
        return this;
    }


    /*** MqttObjectContainer implementation ***************************************************************************/

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
        // Does the object has changes?
        return outOfSync;
    }

    @Override
    public int getImmediateChildrenWithChangesCount() {
        int changes = 0;

        // Count the number of attributes with changes.
        for (MqttAbstractAttribute attribute: attributes) {
            if (attribute.hasChanges()) {
                ++changes;
            }
        }

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

        // Set all sub-objects to be in sync.
        for (MqttObject object: objects) {
            object.setSynchronized();
        }

        // All attributes are in sync now too.
        for (MqttAbstractAttribute attribute: attributes) {
            attribute.setSynchronized();
        }
    }

    @Override
    public void commit(MqttPublisher publisher) {
        // If the object has changes or more than 1 attribute/sub-object has changes, publish the whole object.
        if (hasChanges() || getImmediateChildrenWithChangesCount() > 1) {
            publisher.update(this);
            setSynchronized();
        } else {
            // Call the commit method for all sub-objects.
            for (MqttObject object: objects) {
                object.commit(publisher);
            }

            // Call the commit method on all attributes.
            for (MqttAbstractAttribute attribute: attributes) {
                attribute.commit(publisher);
            }
        }
    }

    @Override
    public UniqueIdentifiable locate(Stack<String> path) throws EmptyStackException {
        // If the path is empty now, it refers to the object itself.
        if (path.isEmpty()) {
            return this;
        } else {
            // Get the child type from the path (can be "objects" or "attributes").
            String childType = path.pop();

            if ("objects".equals(childType)) {
                // If the child type is "objects" search for the object with the given name.
                MqttObject object = objects.getItem(path.pop());

                // If the object exists continue, otherwise return null.
                if (object != null) {
                    return object.locate(path);
                } else {
                    return null;
                }
            } else if ("attributes".equals(childType)) {
                // If the child type is "attributes" search for the attribute with the given name.
                MqttAbstractAttribute attribute = attributes.getItem(path.pop());

                // If the attribute exists continue, otherwise return null.
                if (attribute != null) {
                    return attribute;
                } else {
                    return null;
                }
            } else {
                // If the type is invalid, return null.
                return null;
            }
        }
    }


    /*** JsonSerializable implementation ******************************************************************************/

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Start object.
        gen.writeStartObject();

        // Write conformity, the objects set and the attribute set.
        if (conforms != null) {
            gen.writeObjectField("conforms", conforms);
        }
        if (!objects.isEmtpy()) {
            gen.writeObjectField("objects", objects);
        }
        if (!attributes.isEmtpy()) {
            gen.writeObjectField("attributes", attributes);
        }

        // End object.
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {}
}
