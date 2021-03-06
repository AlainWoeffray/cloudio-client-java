package ch.hevs.cloudio.client.mqtt;

import ch.hevs.cloudio.client.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

class MqttIntegerAttribute extends MqttAbstractAttribute<Integer> implements JsonSerializable {
    private Integer value;

    public MqttIntegerAttribute(String name) {
        super(name);
    }

    @Override
    public Class getType() {
        return Integer.class;
    }

    void setValueFromMqtt(Integer value) {
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
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) throws IllegalArgumentException, IllegalAccessError {
        // Call method with current timestamp in milliseconds.
        setValue(value, (float)System.currentTimeMillis() / 1000f);
    }

    @Override
    public void setValue(Integer value, float timestamp) throws IllegalArgumentException, IllegalAccessError {
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
    public Attribute initialize(Integer value, float timestamp) throws IllegalArgumentException, IllegalStateException {
        if (this.value == null) {
            if (getValidator() == null || getValidator().validate(this, value)) {
                this.value = value;
                this.setTimestamp(timestamp);
            } else {
                throw new IllegalArgumentException("Validator " + getValidator().toString() + " rejected value " +
                        (value == null ? "null" : value.toString()));
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
        gen.writeObjectField("type", "Integer");

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
