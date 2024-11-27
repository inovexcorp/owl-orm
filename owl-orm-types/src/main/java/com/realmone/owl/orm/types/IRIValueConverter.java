/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for creating {@link IRI}s from statements.
 *
 * @author bdgould
 */
public class IRIValueConverter extends AbstractValueConverter<IRI> {

    /**
     * Default constructor.
     */
    public IRIValueConverter() {
        super(IRI.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRI convertValue(@NonNull final Value value)
            throws ValueConversionException {
        if (value instanceof IRI) {
            try {
                return (IRI) value;
            } catch (ClassCastException e) {
                throw new ValueConversionException("Issue casting value '" + value + "' to an IRI.", e);
            }
        } else {
            try {
                return this.valueFactory.createIRI(value.stringValue());
            } catch (Exception e) {
                throw new ValueConversionException("Issue creating IRI from statement.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull final IRI type) throws ValueConversionException {
        return type;
    }
}
