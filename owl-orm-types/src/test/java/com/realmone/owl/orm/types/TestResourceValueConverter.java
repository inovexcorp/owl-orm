/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

public class TestResourceValueConverter extends AbstractValueConverterTest<Resource, Literal> {

    public TestResourceValueConverter() {
        super(VALUE_FACTORY.createIRI("urn://resource"), VALUE_FACTORY.createLiteral("not a resource"),
                new ResourceValueConverter());
    }
}
