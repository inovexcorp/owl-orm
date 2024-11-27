/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;

public class OrmGenerationException extends OrmException {
    
    public OrmGenerationException(String msg) {
        super(msg);
    }

    public OrmGenerationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
