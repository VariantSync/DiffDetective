package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.error.NotImplementedException;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation for metadata that just wraps a single value.
 * TODO: Move this implementation to Functjonal.
 * @param <V> The type of the value to store (e.g., Integer when we want to count something).
 * @param <Derived> The type of the subclass that derives this class.
 * @author Paul Bittner 
 */
public abstract class SimpleMetadata<V, Derived extends SimpleMetadata<V, Derived>> implements Metadata<Derived> {
    public V value;
    private final String valueName;
    private final BiFunction<V, V, V> compose;
    private final Function<String, V> parser;

    /**
     * Create simple metadata with the given default value, name, and composition operator.
     * Creating simple metadata this way, will not implement the parse method, which makes reading
     * a corresponding value from a string crash.
     * If you need parsing, use {@link #SimpleMetadata(Object, String, BiFunction, Function)}
     * @param initialValue initial value of the stored value (e.g., 0 for integer)
     * @param valueName the name this value should have
     * @param compose A binary function which composes two values (e.g., + for integer).
     */
    public SimpleMetadata(
            V initialValue,
            String valueName,
            BiFunction<V, V, V> compose
    ) {
        this(initialValue, valueName, compose, (s) -> {throw new NotImplementedException();});
    }

    /**
     * Create simple metadata with the given default value, name, and composition operator.
     * @param initialValue initial value of the stored value (e.g., 0 for integer)
     * @param valueName the name this value should have
     * @param compose A binary function which composes two values (e.g., + for integer).
     * @param parse A method to parse the corresponding value from a string that contains just the string representation
     *              of the value. This should be inverse to {@code V::toString }.
     */
    protected SimpleMetadata(
            V initialValue,
            String valueName,
            BiFunction<V, V, V> compose,
            Function<String, V> parse
    ) {
        this.value = initialValue;
        this.valueName = valueName;
        this.compose = compose;
        this.parser = parse;
    }


    @Override
    public LinkedHashMap<String, ?> snapshot() {
        final LinkedHashMap<String, Object> snap = new LinkedHashMap<>(1);
        snap.put(valueName, value);
        return snap;
    }

    @Override
    public void setFromSnapshot(LinkedHashMap<String, String> snapshot) {
        this.value = parser.apply(snapshot.get(valueName));
    }

    @Override
    public InplaceSemigroup<Derived> semigroup() {
        return (a, b) -> {
            a.value = compose.apply(a.value, b.value);
        };
    }
}
