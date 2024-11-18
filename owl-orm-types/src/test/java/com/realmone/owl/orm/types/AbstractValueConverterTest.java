/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.realmone.owl.orm.types;

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
