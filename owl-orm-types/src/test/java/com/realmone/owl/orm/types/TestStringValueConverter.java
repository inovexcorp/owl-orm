package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.IRI;

public class TestStringValueConverter extends AbstractValueConverterTest<String, IRI> {

    public TestStringValueConverter() {
        super("goodValue", null, new StringValueConverter());
    }
}