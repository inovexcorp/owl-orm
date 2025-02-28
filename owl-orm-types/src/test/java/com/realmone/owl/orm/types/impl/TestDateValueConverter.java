/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types.impl;

import org.eclipse.rdf4j.model.Literal;

import java.time.OffsetDateTime;

public class TestDateValueConverter extends AbstractValueConverterTest<OffsetDateTime, Literal> {

    public TestDateValueConverter() {
        super(OffsetDateTime.now(), VALUE_FACTORY.createLiteral("not a date"), new DateValueConverter());
    }
}
