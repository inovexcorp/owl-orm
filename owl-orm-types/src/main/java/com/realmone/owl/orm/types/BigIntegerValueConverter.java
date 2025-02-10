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
import org.eclipse.rdf4j.model.base.CoreDatatype;

import java.math.BigInteger;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link BigInteger} objects.
 *
 * @author bdgould
 */
public class BigIntegerValueConverter extends AbstractValueConverter<BigInteger> {

    /**
     * Default constructor.
     */
    public BigIntegerValueConverter() {
        super(BigInteger.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return new BigInteger(value.stringValue());
        } catch (NumberFormatException e) {
            throw new ValueConversionException("Issue getting big integer value from statement.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull BigInteger type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type.toString(), CoreDatatype.XSD.INTEGER);
    }
}
