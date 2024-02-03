package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

public class TestBooleanValueConverter extends AbstractValueConverterTest<Boolean, Literal> {

    public TestBooleanValueConverter() {
        super(true, VALUE_FACTORY.createLiteral("not a boolean"), new BooleanValueConverter());
    }
}
