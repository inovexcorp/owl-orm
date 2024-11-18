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
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Resource}.
 *
 * @author bdgould
 */
public class ResourceValueConverter extends AbstractValueConverter<Resource> {

    /**
     * Default constructor.
     */
    public ResourceValueConverter() {
        super(Resource.class);
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * Try and cast the value to a {@link Resource}, and if this doesn't work,
     * then try and create an IRI from the {@link String} value.
     */
    @Override
    public Resource convertValue(@NonNull final Value value)
            throws ValueConversionException {
        if (value instanceof Resource) {
            try {
                return Resource.class.cast(value);
            } catch (ClassCastException e) {
                throw new ValueConversionException("Issue casting value '" + value + "' into a Resource", e);
            }
        } else {
            try {
                return this.valueFactory.createIRI(value.stringValue());
            } catch (Exception e) {
                throw new ValueConversionException("Issue converting '" + value + "' into a IRI object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Resource type) throws ValueConversionException {
        return type;
    }
}
