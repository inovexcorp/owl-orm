/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import lombok.*;
import org.eclipse.rdf4j.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the base implementation of the {@link ThingFactory} interface. This is the front door for working with OWL
 * ORM API interfaces in your code. It allows the creation of proxy instances of OWL ORM interfaces that will overlay
 * RDF4j models.  Use the builder to create an instance of this class for your solution.
 */
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseThingFactory implements ThingFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseThingFactory.class);

    @NonNull
    private final ValueConverterRegistry valueConverterRegistry;

    @NonNull
    @Getter
    private final ModelFactory modelFactory;

    @NonNull
    @Getter
    private final ValueFactory valueFactory;

    private final ClassLoader[] classLoaders;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T create(Class<T> type, Resource resource, Model model) throws OrmException {
        Type annotation = getTypeAnnotation(type);
        Set<IRI> parents = getAllExtendedOrImplementedTypesRecursively(type).stream()
                .map(parentClazz -> optTypeAnnotation((Class<? extends Thing>) parentClazz))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(parentType -> valueFactory.createIRI(parentType.value()))
                .collect(Collectors.toSet());
        IRI typeIri = valueFactory.createIRI(annotation.value());
        OwlOrmInvocationHandler handler = OwlOrmInvocationHandler.builder()
                .useFactory(this)
                .useValueConverterRegistry(valueConverterRegistry)
                .useModel(model)
                .useDelegate(BaseThing.builder()
                        .useModel(model)
                        .useResource(resource)
                        .useTypeIri(typeIri)
                        .useParents(parents)
                        .useRegistry(valueConverterRegistry)
                        .useCreate(true)
                        .build())
                .build();
        return createInstance(handler, type);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, Resource resource) throws OrmException {
        return create(type, resource, modelFactory.createEmptyModel());
    }

    @Override
    public <T extends Thing> T create(Class<T> type, String resource, Model model) throws OrmException {
        return create(type, valueFactory.createIRI(resource), model);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, String resource) throws OrmException {
        return create(type, valueFactory.createIRI(resource), modelFactory.createEmptyModel());
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model) throws OrmException {
        Type annotation = getTypeAnnotation(type);
        IRI typeIri = valueFactory.createIRI(annotation.value());
        BaseThing delegate = BaseThing.builder()
                .useModel(model)
                .useResource(resource)
                .useTypeIri(typeIri)
                .useRegistry(valueConverterRegistry)
                .useCreate(false)
                .build();
        if (delegate.isDetached()) {
            return Optional.empty();
        } else {
            OwlOrmInvocationHandler handler = OwlOrmInvocationHandler.builder()
                    .useFactory(this)
                    .useValueConverterRegistry(valueConverterRegistry)
                    .useModel(model)
                    .useDelegate(delegate)
                    .build();
            return Optional.of(createInstance(handler, type));
        }
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, String resource, Model model) throws OrmException {
        return get(type, valueFactory.createIRI(resource), model);
    }

    /**
     * Get the type annotation for a given {@link Class} representing a OWL ORM {@link Thing}.
     *
     * @param type The type of {@link Class} being inspected
     * @param <T>  The type of {@link Class} that was passed in
     * @return The {@link Type} annotation on that {@link Class} or an {@link IllegalStateException}
     */
    private static <T extends Thing> Type getTypeAnnotation(Class<T> type) {
        return optTypeAnnotation(type).orElseThrow(() ->
                new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName()));
    }

    private static <T extends Thing> Optional<Type> optTypeAnnotation(Class<T> type) {
        return Optional.ofNullable(type.getDeclaredAnnotation(Type.class));
    }

    /**
     * This class will recursively get a set of all the {@link Class}es that are extended or interfaces implemented
     * by a given root {@link Class}.
     *
     * @param clazz The root {@link Class} to start from
     * @return The {@link Set} of {@link Class} entities that affect the root {@link Class} hierarchy.
     */
    public static Set<Class<?>> getAllExtendedOrImplementedTypesRecursively(final Class<?> clazz) {
        return walk(clazz)
                .filter(Predicate.isEqual(clazz).negate())
                .filter(Predicate.isEqual(Thing.class).negate())
                .collect(Collectors.toSet());
    }

    /**
     * This method simply will return a stream that contains the ancestors/class hierarchy for a given {@link Class}.
     *
     * @param clazz The root {@link Class} to walk
     * @return A {@link Stream} of {@link Class} entities that represent the class and its ancestors
     */
    public static Stream<Class<?>> walk(final Class<?> clazz) {
        // Return the concatenated stream of the root class
        return Stream.concat(Stream.of(clazz),
                // Concatenate two other streams :)
                Stream.concat(
                        // include the parent/super class (extends)
                        Optional.ofNullable(clazz.getSuperclass()).stream(),
                        // and all the interfaces the class implements
                        Arrays.stream(clazz.getInterfaces())
                ).flatMap(BaseThingFactory::walk));
    }

    /**
     * Attempts to create an instance of the provided class using the provided {@link OwlOrmInvocationHandler}. Tries
     * the provided ClassLoaders if present and falls back on the loader for the handler.
     *
     * @param handler The specific {@link OwlOrmInvocationHandler} to use when creating the instance
     * @param type The type of Thing to create
     * @return The Thing instance created
     * @param <T> A type of Thing
     */
    @SuppressWarnings("unchecked")
    private <T extends Thing> T createInstance(OwlOrmInvocationHandler handler, Class<T> type) {
        if (classLoaders != null) {
            LOGGER.trace("Trying to create an instance of {} using provided class loaders", type);
            for (ClassLoader classLoader: classLoaders) {
                String id = String.valueOf(System.identityHashCode(classLoader));
                try {
                    LOGGER.trace("Trying class loader {}", id);
                    return (T) Proxy.newProxyInstance(classLoader, new Class[]{type}, handler);
                } catch (IllegalArgumentException ex) {
                    LOGGER.trace("Could not create instance with class loader {}", id);
                    ex.printStackTrace();
                }
            }
        }
        return (T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler);
    }
}
