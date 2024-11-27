/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;

public class TestBooleanValueConverter extends AbstractValueConverterTest<Boolean, Literal> {

    public TestBooleanValueConverter() {
        super(true, VALUE_FACTORY.createLiteral("not a boolean"), new BooleanValueConverter());
    }
}
