/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.annotations;

import java.lang.annotation.*;

/**
 * This method annotates a given OWL ORM API interface to help our proxy engine work with instances of this
 * interface and overlay an RDF4j {@link org.eclipse.rdf4j.model.Model}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {

    String value();
}
