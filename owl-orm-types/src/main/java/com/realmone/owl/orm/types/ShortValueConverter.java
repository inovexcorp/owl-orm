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
