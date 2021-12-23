package util.semigroup;

import de.variantsync.functjonal.Cast;

public record Unknown(Semigroup<?> val) implements Semigroup<Unknown> {
    @Override
    public void append(Unknown other) {
        if (val.getClass().equals(other.val.getClass())) {
            val.append(Cast.unchecked(other.val));
        } else {
            throw new MergeMap.CannotAppend("Tried to append values of different types: " + val.getClass() + " and " + other.val.getClass());
        }
    }

    @Override
    public String toString() {
        return val.toString();
    }
}
