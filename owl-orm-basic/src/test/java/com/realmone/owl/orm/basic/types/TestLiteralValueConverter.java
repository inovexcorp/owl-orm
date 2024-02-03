package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class TestLiteralValueConverter extends AbstractValueConverterTest<Literal, IRI> {

    public TestLiteralValueConverter() {
        super(VALUE_FACTORY.createLiteral("123"), VALUE_FACTORY.createIRI("urn://not.a.literal"),
                new LiteralValueConverter());
    }
}
