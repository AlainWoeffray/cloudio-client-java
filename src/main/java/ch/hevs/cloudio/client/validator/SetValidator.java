package ch.hevs.cloudio.client.validator;

import ch.hevs.cloudio.client.Attribute;
import ch.hevs.cloudio.client.AttributeValidator;

/**
 * Checks that the new value is part of a given set of values. If not, the value will be rejected.
 */
public class SetValidator<T extends Comparable> implements AttributeValidator<T> {
    private T[] possibleValues;

    /**
     * Creates a new validator only accepting the given set of values.
     *
     * @param possibleValues    Set of values allowed by the validator.
     */
    public SetValidator(T... possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public boolean validate(Attribute<T> attribute, T newValue) {
        try {
            for (T possibleValue : possibleValues) {
                if (possibleValue.compareTo(newValue) == 0) return true;
            }
        } catch (ClassCastException e) {}

        return false;
    }

    @Override
    public String toString() {
        String output = "[ ";
        for (T possibleValue: possibleValues) {
            output += "\"" + possibleValue.toString() + "\" ";
        }
        output += "]";
        return output;
    }
}
