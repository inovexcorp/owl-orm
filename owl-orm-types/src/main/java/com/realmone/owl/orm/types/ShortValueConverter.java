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

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Short}s.
 *
 * @author bdgould
 */
public class ShortValueConverter extends AbstractValueConverter<Short> {

    /**
     * Construct a new {@link ShortValueConverter}.
     */
    public ShortValueConverter() {
        super(Short.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Short convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            return Short.parseShort(value.stringValue());
        } catch (NumberFormatException e) {
            throw new ValueConversionException("Issue getting short value from statement", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Short type) throws ValueConversionException {
        return this.valueFactory.createLiteral(type);
    }
}
