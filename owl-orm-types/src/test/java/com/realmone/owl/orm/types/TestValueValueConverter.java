/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Value;

public class TestValueValueConverter extends AbstractValueConverterTest<Value, Value> {

    public TestValueValueConverter() {
        super(VALUE_FACTORY.createIRI("urn://value"), null, new ValueValueConverter());
    }
}
