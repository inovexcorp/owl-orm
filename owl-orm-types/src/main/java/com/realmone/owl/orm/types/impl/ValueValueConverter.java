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
