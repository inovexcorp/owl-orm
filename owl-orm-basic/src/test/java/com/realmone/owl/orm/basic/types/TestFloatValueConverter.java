package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

public class TestFloatValueConverter extends AbstractValueConverterTest<Float, Literal> {

    public TestFloatValueConverter() {
        super(3.1415F, VALUE_FACTORY.createLiteral("not a float"), new FloatValueConverter());
    }
}
