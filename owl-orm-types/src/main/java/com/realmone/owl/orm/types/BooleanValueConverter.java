package com.realmone.owl.orm.types;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} implementation for converting {@link Boolean} values from statements.
 */
public class BooleanValueConverter extends AbstractValueConverter<Boolean> {

    /**
     * Create a new instance of a {@link BooleanValueConverter}.
     */
    public BooleanValueConverter() {
        super(Boolean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean convertValue(@NonNull final Value value) throws ValueConversionException {
        try {
            return ((Literal) value).booleanValue();
        } catch (Exception e) {
            throw new ValueConversionException("Issue converting '" + value.stringValue() + "' to boolean", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull final Boolean value) throws ValueConversionException {
        return this.valueFactory.createLiteral(value);
    }
}
