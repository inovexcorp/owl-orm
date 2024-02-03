package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Value;

public class TestValueValueConverter extends AbstractValueConverterTest<Value, Value> {

    public TestValueValueConverter() {
        super(VALUE_FACTORY.createIRI("urn://value"), null, new ValueValueConverter());
    }
}
