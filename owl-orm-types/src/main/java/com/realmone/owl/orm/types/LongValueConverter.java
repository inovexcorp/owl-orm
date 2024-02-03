package com.realmone.owl.orm.types;

import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;


public class LongValueConverter extends AbstractValueConverter<Long> {

    public LongValueConverter() {
        super(Long.class);
    }

    @Override
    public Long convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return ((Literal) value).longValue();
        } catch (Exception e) {
            throw new ValueConversionException("Issue converting value '" + value + "' into a long", e);
        }
    }

    @Override
    public Value convertType(@NonNull Long type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }
}
