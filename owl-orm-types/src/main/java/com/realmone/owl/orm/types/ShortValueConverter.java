package com.realmone.owl.orm.types;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Short}s.
 *
 * @author bdgould
 */
public class ShortValueConverter extends AbstractValueConverter<Short> {

    /**
     * Construct a new {@link ShortValueConverter}.
     */
    public ShortValueConverter() {
        super(Short.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return Short.parseShort(value.stringValue());
        } catch (NumberFormatException e) {
            throw new ValueConversionException("Issue getting short value from statement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Short type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }
}
