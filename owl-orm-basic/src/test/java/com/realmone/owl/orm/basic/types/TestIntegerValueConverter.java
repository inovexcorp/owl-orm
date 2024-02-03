package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

public class TestIntegerValueConverter extends AbstractValueConverterTest<Integer, Literal> {

    public TestIntegerValueConverter() {
        super(123, VALUE_FACTORY.createLiteral("not an int"), new IntegerValueConverter());
    }
}
