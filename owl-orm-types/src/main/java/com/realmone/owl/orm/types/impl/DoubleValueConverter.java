/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types.impl;

import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Double} types.
 *
 * @author bdgould
 */
public class DoubleValueConverter extends AbstractValueConverter<Double> {

    /**
     * Construct a new {@link DoubleValueConverter}.
     */
    public DoubleValueConverter() {
        super(Double.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return Double.parseDouble(value.stringValue());
        } catch (NumberFormatException e) {
            throw new ValueConversionException("Issue getting double value from statement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Double type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }
}
