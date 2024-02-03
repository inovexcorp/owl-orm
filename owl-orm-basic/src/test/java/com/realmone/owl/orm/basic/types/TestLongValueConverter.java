package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

public class TestLongValueConverter extends AbstractValueConverterTest<Long, Literal> {

    public TestLongValueConverter() {
        super(123L, VALUE_FACTORY.createLiteral("not a long"), new LongValueConverter());
    }
}
