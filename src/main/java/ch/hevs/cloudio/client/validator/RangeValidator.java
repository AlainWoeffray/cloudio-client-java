package ch.hevs.cloudio.client.validator;

import ch.hevs.cloudio.client.Attribute;
import ch.hevs.cloudio.client.AttributeValidator;

/**
 * Checks that the number is in a given valid range. This implies that the datatype of the value and the range has
 * to be of type Comparable. If the value is not Comparable, the validator returns false and thus rejects the new
 * value.
 */
public class RangeValidator<T extends Comparable> implements AttributeValidator<T> {
    private T min, max;

    /**
     * Creates a new range validator that starts at min (including min) and ends with max (including max).
     *
     * @param min   Minimal value.
     * @param max   Maximal value.
     */
    public RangeValidator(T min, T max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean validate(Attribute<T> attribute, T newValue) {
        try {
            return min.compareTo(newValue) <= 0 && max.compareTo(newValue) >= 0;
        } catch (ClassCastException e) {}

        return false;
    }

    @Override
    public String toString() {
        return "[" + min.toString() + ", " + max.toString() +"]";
    }
}
