package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;

import java.time.OffsetDateTime;

public class TestDateValueConverter extends AbstractValueConverterTest<OffsetDateTime, Literal> {

    public TestDateValueConverter() {
        super(OffsetDateTime.now(), VALUE_FACTORY.createLiteral("not a date"), new DateValueConverter());
    }
}
