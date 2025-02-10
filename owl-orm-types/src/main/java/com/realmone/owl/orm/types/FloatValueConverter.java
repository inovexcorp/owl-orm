/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Float}s.
 *
 * @author bdgould
 */
public class FloatValueConverter extends AbstractValueConverter<Float> {

    /**
     * Construct a new {@link FloatValueConverter}.
     */
    public FloatValueConverter() {
        super(Float.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return Float.parseFloat(value.stringValue());
        } catch (NumberFormatException e) {
            throw new ValueConversionException("Issue getting float value from statement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Float type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }
}
