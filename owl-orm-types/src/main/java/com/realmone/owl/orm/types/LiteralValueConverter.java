package com.realmone.owl.orm.types;

import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Literal}s.
 *
 * @author bdgould
 */
public class LiteralValueConverter extends AbstractValueConverter<Literal> {

    /**
     * Construct a new {@link LiteralValueConverter}.
     */
    public LiteralValueConverter() {
        super(Literal.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return Literal.class.cast(value);
        } catch (ClassCastException e) {
            throw new ValueConversionException("Issue creating literal from value specified: " + value, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Literal type) throws ValueConversionException {
        return type;
    }
}
