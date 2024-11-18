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

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;

import java.util.Optional;

/**
 * This interface describes the front door to workign with the OWL ORM API. To create instances of your types
 * of {@link Thing}s, simple use a {@link ThingFactory} implementation to get/create them.
 */
public interface ThingFactory {

    /**
     * Create a new intance of your type of {@link Thing}.
     *
     * @param type     The interface class that extends {@link Thing} that you want to work with
     * @param resource The {@link Resource} that uniquely identifies your instance of the {@link Thing}
     * @param model    The {@link Model} to use to underpin your {@link Thing}
     * @param <T>      The type of {@link Thing} you want to work with
     * @return The instance of your {@link Thing}
     * @throws OrmException If a {@link Thing} already exists with the {@link Resource} you tried to create
     */
    <T extends Thing> T create(Class<T> type, Resource resource, Model model) throws OrmException;

    /**
     * Create a new intance of your type of {@link Thing}.
     *
     * @param type     The interface class that extends {@link Thing} that you want to work with
     * @param resource The String resource that uniquely identifies your instance of the {@link Thing}
     * @param model    The {@link Model} to use to underpin your {@link Thing}
     * @param <T>      The type of {@link Thing} you want to work with
     * @return The instance of your {@link Thing}
     * @throws OrmException If a {@link Thing} already exists with the {@link Resource} you tried to create
     */
    <T extends Thing> T create(Class<T> type, String resource, Model model) throws OrmException;

    /**
     * Get a {@link Thing} that should exist in the underling {@link Model} you're working with.
     *
     * @param type     The interface class that extends {@link Thing} that you want to work with
     * @param resource The {@link Resource} that uniquely identifies your instance of the {@link Thing}
     * @param model    The {@link Model} to use to underpin your {@link Thing}
     * @param <T>      The type of {@link Thing} you want to work with
     * @return The instance of your {@link Thing} or an empty {@link Optional} if it doesn't exist in your {@link Model}
     * @throws OrmException If there is an issue with getting your instance
     */
    <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model) throws OrmException;

    /**
     * @return The RDF4j {@link ValueFactory} backing this {@link ThingFactory}.
     */
    ValueFactory getValueFactory();

    /**
     * @return The RDF4j {@link ModelFactory} backing this {@link ThingFactory}.
     */
    ModelFactory getModelFactory();
}
