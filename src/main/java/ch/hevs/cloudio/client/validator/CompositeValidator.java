package ch.hevs.cloudio.client.validator;

import ch.hevs.cloudio.client.Attribute;
import ch.hevs.cloudio.client.AttributeValidator;

/**
 * Allows to group multiple validators into one AttributeValidator facade.
 */
public class CompositeValidator<T> implements AttributeValidator<T> {
    private AttributeValidator<T>[] validators;

    /**
     * Creates a new composite validator with the given validators.
     *
     * @param validators    One or more validators to group inside this composite validator.
     */
    public CompositeValidator(AttributeValidator... validators) {
        this.validators = validators;
    }

    @Override
    public boolean validate(Attribute<T> attribute, T newValue) {
        for (AttributeValidator validator: validators) {
            if (!validator.validate(attribute, newValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        String output = "{";
        for (AttributeValidator validator: validators) {
            output += validator.toString();
        }
        output += "}";
        return output;
    }
}
