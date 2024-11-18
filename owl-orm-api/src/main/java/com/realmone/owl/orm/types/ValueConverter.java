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

public interface ValueConverter<T> {

    /**
     * Convert a value to the specified type.
     *
     * @param value The {@link Value} to convert
     * @return The converted instance
     * @throws ValueConversionException If there is an issue converting the value
     */
    T convertValue(@NonNull Value value) throws ValueConversionException;

    /**
     * Convert an instance of the TYPE of object this {@link ValueConverter} works with back into a {@link Value}.
     *
     * @param type The object to convert into a {@link Value}
     * @return The {@link Value} form of the object passed in
     * @throws ValueConversionException If there is an issue performing the conversion
     */
    Value convertType(@NonNull T type) throws ValueConversionException;

    /**
     * @return The type of class this convert works with
     */
    Class<T> getType();
}
