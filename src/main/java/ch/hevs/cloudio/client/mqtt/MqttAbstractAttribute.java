package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @todo : doc...
 */
public abstract class MqttAbstractAttribute<T> implements Attribute<T>, MqttSynchronizable {
    private MqttObject parent = null;
    private String name;

    private AttributeConstraint constraint = AttributeConstraint.UNSPECIFIED;
    private float timestamp = 0f;

    private boolean outOfSync = true;
    private AttributeValidator validator = null;
    private List<AttributeListener> listeners = new LinkedList<AttributeListener>();

    public MqttAbstractAttribute(String name) {
        this.name = name;
    }

    public abstract Class getType();

    public MqttObject getParent() {
        return parent;
    }

    public void setParent(MqttObject parent) {
        this.parent = parent;
    }

    protected AttributeValidator getValidator() {
        return validator;
    }

    public float getTimestamp() {
        return timestamp;
    }

    protected void setTimestamp(float timestamp) {
        this.timestamp = timestamp;
    }

    protected void setOutOfSync() {
        this.outOfSync = true;
    }

    protected List<AttributeListener> getListeners() {
        return listeners;
    }


    /*** Attribute Implementation *************************************************************************************/

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
        return uuid;
    }

    @Override
    public AttributeConstraint getConstraint() {
        return constraint;
    }

    @Override
    public Attribute constraint(AttributeConstraint constraint) throws IllegalArgumentException {
        // UNSPECIFIED is not allowed!
        if (constraint == AttributeConstraint.UNSPECIFIED)
            throw new IllegalArgumentException("Constraint can not be set to " +
                    AttributeConstraint.UNSPECIFIED.toString());

        this.constraint = constraint;
        return this;
    }

    @Override
    public float getCurrentValueTimestamp() {
        return timestamp;
    }

    @Override
    public Attribute validator(AttributeValidator attributeValidator) {
        validator = attributeValidator;
        return this;
    }

    @Override
    public void addAttributeListener(AttributeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeAttributeListener(AttributeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }


    /*** MqttSynchronizable implementation ****************************************************************************/

    @Override
    public boolean hasChanges() {
        // If it is out of sync, an attribute has actual changes.
        return outOfSync;
    }

    @Override
    public int getImmediateChildrenWithChangesCount() {
        // As an attribute never has children, this will be always 0.
        return 0;
    }

    @Override
    public void setSynchronized() {
        // When the data has been synchronized with the cloud, we can consider the value as up to date.
        outOfSync = false;
    }

    @Override
    public void commit(MqttPublisher publisher) {
        // Does the attribute has been changed since the last update?
        if (hasChanges()) {
            // Yes, so publish the attribute.
            publisher.update(this);

            // Mark it as in sync.
            setSynchronized();
        }
    }
}
