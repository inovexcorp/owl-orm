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

import com.realmone.owl.orm.OrmException;

import java.io.Serial;
import java.util.List;

public class ValueConversionException extends OrmException {
    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 1853010493451281919L;

    /**
     * Construct a new {@link ValueConversionException}.
     *
     * @param msg The message to associate with this
     *            {@link ValueConversionException}
     */
    public ValueConversionException(final String msg) {
        super(msg);
    }

    /**
     * Construct a new {@link ValueConversionException} with the given message
     * and cause.
     *
     * @param msg   The message to associate with this
     *              {@link ValueConversionException}
     * @param cause The underlying cause of this {@link ValueConversionException}
     */
    public ValueConversionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Construct a new {@link ValueConversionException}.
     *
     * @param msg    The message to associate with this
     *               {@link ValueConversionException}
     * @param causes A {@link List} of {@link Throwable} causes
     */
    public ValueConversionException(final String msg, final List<Exception> causes) {
        super(msg + "\n\t" + join(causes.stream().map(Throwable::getMessage).toList(), "\n\t"));
    }

    /**
     * Join a {@link Iterable} list of messages into a new {@link String}.
     *
     * @param target    The target {@link Iterable} of {@link String}s
     * @param separator A separator for each item in the {@link Iterable}
     * @return The new message {@link String}
     */
    public static String join(final Iterable<String> target, final String separator) {
        final StringBuilder sb = new StringBuilder();
        target.forEach(val -> {
            if (!sb.isEmpty()) {
                sb.append(separator);
            }
            sb.append(val);
        });
        return sb.toString();
    }
}
