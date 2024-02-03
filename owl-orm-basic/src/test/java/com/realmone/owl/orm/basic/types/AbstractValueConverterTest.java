package com.realmone.owl.orm.basic.types;

import com.realmone.owl.orm.types.ValueConversionException;
import com.realmone.owl.orm.types.ValueConverter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractValueConverterTest<X, Y extends Value> {

    protected static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    protected final X goodValue;
    protected final Y badValue;
    protected final ValueConverter<X> converter;

    @Test
    public void simpleTest() throws ValueConversionException {
        final Value value = converter.convertType(goodValue);
        X after = converter.convertValue(value);
        Assert.assertTrue(String.format("After value '%s' not the same as '%s'", after, goodValue),
                valuesEqual(goodValue, after));
    }

    @Test(expected = ValueConversionException.class)
    public void testBadValueConversion() throws Exception {
        if (badValue != null) {
            converter.convertValue(badValue);
        } else {
            System.err.println("No bad value passed in, so we're skipping real evaluation of this test...:\n\t"
                    + "Type of good value: " + goodValue.getClass());
            throw new ValueConversionException("Throwing to pass test when no bad value configuration is present");
        }
    }

    /**
     * Allows overriding of the equality check if necessary for a given type (e.g. Calendar).
     *
     * @param before The value that went into the ValueConverter.
     * @param after  The value that came out after being converted back and forth.
     * @return Whether or not the two values are the same semantically.
     */
    protected boolean valuesEqual(X before, X after) {
        return Objects.equals(before, after);
    }
}
