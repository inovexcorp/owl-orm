package com.realmone.owl.orm;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

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

}
