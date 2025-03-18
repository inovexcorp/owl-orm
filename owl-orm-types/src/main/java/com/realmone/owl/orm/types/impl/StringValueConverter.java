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
