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
package com.realmone.owl.orm;

import java.io.Serial;

public class OrmException extends RuntimeException {

    /**
     * Serial version UID.
     */
    @Serial
    private static final long serialVersionUID = -411409811095531213L;

    /**
     * Construct a new {@link OrmException}.
     *
     * @param msg The message to attach
     */
    public OrmException(final String msg) {
        super(msg);
    }

    /**
     * Construct a new {@link OrmException}.
     *
     * @param msg   The message to attach
     * @param cause The underlying cause to attach
     */
    public OrmException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
