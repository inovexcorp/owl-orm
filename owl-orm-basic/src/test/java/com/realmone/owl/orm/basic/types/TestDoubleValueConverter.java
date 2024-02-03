package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

public class TestDoubleValueConverter extends AbstractValueConverterTest<Double, Literal> {

    public TestDoubleValueConverter() {
        super(3.1415, VALUE_FACTORY.createLiteral("not a double"), new DoubleValueConverter());
    }
}
