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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;

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

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    @NonNull
    private final ValueConverterRegistry valueConverterRegistry;

    @NonNull
    @Getter
    private final ModelFactory modelFactory;

    @NonNull
    @Getter
    private final ValueFactory valueFactory;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T create(Class<T> type, Resource resource, Model model) throws OrmException {
        Type annotation = getTypeAnnotation(type);
        Set<IRI> parents = getAllExtendedOrImplementedTypesRecursively(type).stream()
                .map(parentClazz -> getTypeAnnotation((Class<? extends Thing>) parentClazz))
                .map(parentType -> VALUE_FACTORY.createIRI(parentType.value()))
                .collect(Collectors.toSet());
        parents.forEach(System.out::println);
        IRI typeIri = VALUE_FACTORY.createIRI(annotation.value());
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
        return (T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler);
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
    @SuppressWarnings("unchecked")
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
            return Optional.of((T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                    new Class[]{type}, handler));
        }
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, String resource, Model model) throws OrmException {
        return get(type, valueFactory.createIRI(resource), model);
    }

    private <T extends Thing> Type getTypeAnnotation(Class<T> type) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);
        if (typeAnn == null) {
            throw new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName());
        }
        return typeAnn;
    }

    public Set<Class<?>> getAllExtendedOrImplementedTypesRecursively(final Class<?> clazz) {
        return walk(clazz)
                .filter(Predicate.isEqual(clazz).negate())
                .filter(Predicate.isEqual(Thing.class).negate())
                .collect(Collectors.toSet());
    }

    public Stream<Class<?>> walk(final Class<?> c) {
        return Stream.concat(Stream.of(c),
                Stream.concat(
                        Optional.ofNullable(c.getSuperclass()).stream(),
                        Arrays.stream(c.getInterfaces())
                ).flatMap(this::walk));
    }
}
