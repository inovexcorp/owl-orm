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
