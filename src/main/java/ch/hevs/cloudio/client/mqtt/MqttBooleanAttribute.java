package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.Attribute;
import ch.hevs.cloudio.client.AttributeConstraint;
import ch.hevs.cloudio.client.AttributeListener;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

class MqttBooleanAttribute extends MqttAbstractAttribute<Boolean> implements JsonSerializable {
    private Boolean value;

    public MqttBooleanAttribute(String name) {
        super(name);
    }

    @Override
    public Class getType() {
        return Boolean.class;
    }

    void setValueFromMqtt(Boolean value) {
        // Only PARAMETER and SET_POINT can be changed from remote.
        if (getConstraint() == AttributeConstraint.PARAMETER || getConstraint() == AttributeConstraint.SET_POINT) {
            // Let the validator check the value before actually applying it.
            if (getValidator() == null || getValidator().validate(this, value)) {
                // Apply new value and update timestamp with current system timestamp.
                this.value = value;
                this.setTimestamp(System.currentTimeMillis() / 1000f);

                // The value is out of sync.
                this.setOutOfSync();

                // Inform all attribute listeners about the change.
                for (AttributeListener listener : getListeners()) {
                    listener.attributeChanged(this);
                }

                // Inform parent about the change.
                if (getParent() != null) {
                    getParent().attributeChanged(this);
                }
            }
        }
    }


    /*** Attribute Implementation *************************************************************************************/


    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean value) throws IllegalArgumentException, IllegalAccessError {
        // Call method with current timestamp in milliseconds.
        setValue(value, (float)System.currentTimeMillis() / 1000f);
    }

    @Override
    public void setValue(Boolean value, float timestamp) throws IllegalArgumentException, IllegalAccessError {
        // Only attributes with constraint STATUS, MEASURE and initial value for all others can be updated from endpoint
        // context.
        if (getConstraint() == AttributeConstraint.STATUS || getConstraint() == AttributeConstraint.MEASURE) {
            // First validate value before actually applying it.
            if (getValidator() == null || getValidator().validate(this, value)) {
                // Update value, timestamp and mark the attribute as out of sync.
                this.value = value;
                this.setTimestamp(timestamp);
                this.setOutOfSync();

                // Notify parent (object) about the change.
                if (getParent() != null) {
                    getParent().attributeChanged(this);
                }
            } else {
                throw new IllegalArgumentException("Validator " + getValidator().toString() + " rejected value " +
                        (value  == null ? "null" : value.toString()));
            }
        } else {
            throw new IllegalAccessError("Only attributes with constraints STATUS, MEASURE and STATIC " +
                    "(only for initial values) can be set during runtime.");
        }
    }

    @Override
    public <T> Attribute initialize(T value, float timestamp) throws IllegalArgumentException, IllegalStateException {
        if (this.value == null) {

            // TODO: Maybe we should add another variable to indicate that the attribute was not initialized yet.
            // TODO: Check if online...

            if (value instanceof Boolean) {
                if (getValidator() == null || getValidator().validate(this, value)) {
                    this.value = (Boolean)value;
                    this.setTimestamp(timestamp);
                } else {
                    throw new IllegalArgumentException("Validator " + getValidator().toString() + " rejected value " +
                            (value == null ? "null" : value.toString()));
                }
            } else {
                throw new IllegalArgumentException("A variable of type " + this.value.getClass().getName() +
                        " can not be initialized with " + value.getClass().getName() + "!");
            }
        } else {
            throw new IllegalStateException("Attribute can not be initialized twice!");
        }

        return this;
    }


    /*** JsonSerializable implementation ******************************************************************************/

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Start attribute object.
        gen.writeStartObject();

        // Write datatype.
        gen.writeObjectField("type", "Boolean");

        // Write constraint.
        gen.writeObjectField("constraint", getConstraint());

        // If the constraint is STATIC, the timestamp is null otherwise include the timestamp.
        if (getConstraint() != AttributeConstraint.STATIC && getTimestamp() != 0) {
            gen.writeNumberField("timestamp", getTimestamp());
        }

        // Try to serialize the value.
        try {
            gen.writeObjectField("value", value);
        } catch (Exception exception) {
            gen.writeString("ERROR: Attribute value not serializable!");
        }

        // Write object end tag.
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
            throws IOException {}
}
