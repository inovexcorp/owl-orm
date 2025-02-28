/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types.impl;

import org.eclipse.rdf4j.model.Literal;

public class TestFloatValueConverter extends AbstractValueConverterTest<Float, Literal> {

    public TestFloatValueConverter() {
        super(3.1415F, VALUE_FACTORY.createLiteral("not a float"), new FloatValueConverter());
    }
}
