/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;

public class TestLongValueConverter extends AbstractValueConverterTest<Long, Literal> {

    public TestLongValueConverter() {
        super(123L, VALUE_FACTORY.createLiteral("not a long"), new LongValueConverter());
    }
}
