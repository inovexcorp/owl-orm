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
