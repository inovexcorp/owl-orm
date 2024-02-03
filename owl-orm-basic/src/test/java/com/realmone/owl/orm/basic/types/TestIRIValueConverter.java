package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class TestIRIValueConverter extends AbstractValueConverterTest<IRI, Literal> {

    public TestIRIValueConverter() {
        super(VALUE_FACTORY.createIRI("urn://test"), VALUE_FACTORY.createLiteral("not an iri"),
                new IRIValueConverter());
    }
}
