package com.realmone.owl.orm.types;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Integer}s.
 *
 * @author bdgould
 */
public class IntegerValueConverter extends AbstractValueConverter<Integer> {

    /**
     * Construct a new {@link IntegerValueConverter}.
     */
    public IntegerValueConverter() {
        super(Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer convertValue(@NonNull final Value value)
            throws ValueConversionException {
        try {
            return Integer.parseInt(value.stringValue());
        } catch (NumberFormatException e) {
            throw new ValueConversionException("Issue getting int value from statement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Integer type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }
}
