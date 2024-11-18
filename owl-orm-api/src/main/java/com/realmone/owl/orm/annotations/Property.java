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
package com.realmone.owl.orm.annotations;

import org.eclipse.rdf4j.model.Value;

import java.lang.annotation.*;

/**
 * This annotation is a hint for the OWL ORM Engine to be able to work with generated proxy instances of the
 * interfaces.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    /**
     * @return The {@link String} representation of the property IRI for this method.
     */
    String value();

    /**
     * @return Whether this method overlays a functional property.
     */
    boolean functional() default false;

    /**
     * @return The Java type that this method will work with as a hint to the
     * {@link com.realmone.owl.orm.types.ValueConverter} system.
     */
    Class<?> type() default Value.class;
}
