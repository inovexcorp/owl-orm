/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
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
