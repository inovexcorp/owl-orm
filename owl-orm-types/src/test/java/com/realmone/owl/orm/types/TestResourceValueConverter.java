package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

public class TestResourceValueConverter extends AbstractValueConverterTest<Resource, Literal> {

    public TestResourceValueConverter() {
        super(VALUE_FACTORY.createIRI("urn://resource"), VALUE_FACTORY.createLiteral("not a resource"),
                new ResourceValueConverter());
    }
}
