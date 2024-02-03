package com.realmone.owl.orm.basic.types;

import org.eclipse.rdf4j.model.Literal;

import java.math.BigInteger;

public class TestBigIntegerValueConverter extends AbstractValueConverterTest<BigInteger, Literal> {

    public TestBigIntegerValueConverter() {
        super(BigInteger.TEN, VALUE_FACTORY.createLiteral("not a big int"), new BigIntegerValueConverter());
    }
}
