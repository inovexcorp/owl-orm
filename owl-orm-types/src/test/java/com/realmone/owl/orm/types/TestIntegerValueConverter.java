package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;

public class TestIntegerValueConverter extends AbstractValueConverterTest<Integer, Literal> {

    public TestIntegerValueConverter() {
        super(123, VALUE_FACTORY.createLiteral("not an int"), new IntegerValueConverter());
    }
}
