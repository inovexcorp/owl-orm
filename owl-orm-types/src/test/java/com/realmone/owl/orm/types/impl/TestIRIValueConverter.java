/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class TestIRIValueConverter extends AbstractValueConverterTest<IRI, Literal> {

    public TestIRIValueConverter() {
        super(VALUE_FACTORY.createIRI("urn://test"), VALUE_FACTORY.createLiteral("not an iri"),
                new IRIValueConverter());
    }
}
