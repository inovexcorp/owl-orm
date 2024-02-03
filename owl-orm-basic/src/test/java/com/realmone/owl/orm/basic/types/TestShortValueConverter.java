package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

public class TestShortValueConverter extends AbstractValueConverterTest<Short, Literal> {

    public TestShortValueConverter() {
        super(Short.MAX_VALUE, VALUE_FACTORY.createLiteral("not a short"), new ShortValueConverter());
    }
}
