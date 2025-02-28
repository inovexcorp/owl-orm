/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types.impl;

import org.eclipse.rdf4j.model.Literal;

public class TestShortValueConverter extends AbstractValueConverterTest<Short, Literal> {

    public TestShortValueConverter() {
        super(Short.MAX_VALUE, VALUE_FACTORY.createLiteral("not a short"), new ShortValueConverter());
    }
}
