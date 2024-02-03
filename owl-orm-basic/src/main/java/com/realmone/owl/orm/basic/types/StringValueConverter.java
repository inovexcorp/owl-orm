package com.realmone.owl.orm.basic.types;

import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link String}s.
 *
 * @author bdgould
 */
public class StringValueConverter extends AbstractValueConverter<String> {

    /**
     * Construct a new {@link StringValueConverter}.
     */
    public StringValueConverter() {
        super(String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertValue(@NonNull Value value) throws ValueConversionException {
        return value.stringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull String type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
