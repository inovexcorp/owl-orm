package com.realmone.owl.orm.types;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Value}s...
 *
 * @author bdgould
 */
public class ValueValueConverter extends AbstractValueConverter<Value> {

    /**
     * Construct a new {@link ValueValueConverter}.
     */
    public ValueValueConverter() {
        super(Value.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertValue(@NonNull Value value)
            throws ValueConversionException {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Value type) throws ValueConversionException {
        return type;
    }
}
