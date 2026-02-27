/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.TypeMetadata;
import com.realmone.owl.orm.TypeRegistry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple, thread-safe implementation of {@link TypeRegistry} that stores type metadata
 * in concurrent hash maps.
 * <p>
 * This implementation is suitable for both OSGi and non-OSGi environments. In OSGi environments,
 * use the {@link #register(TypeMetadata, String)} method to associate types with their source
 * bundle, allowing all types from a bundle to be unregistered when the bundle stops.
 * <p>
 * All lookup operations are thread-safe and can be performed concurrently with registrations.
 */
public class SimpleTypeRegistry implements TypeRegistry {

    /**
     * Maps Java class to its metadata.
     */
    private final Map<Class<? extends Thing>, TypeMetadata<? extends Thing>> typesByClass =
            new ConcurrentHashMap<>();

    /**
     * Maps type IRI to its metadata.
     */
    private final Map<IRI, TypeMetadata<? extends Thing>> typesByIRI =
            new ConcurrentHashMap<>();

    /**
     * Maps source identifiers (e.g., bundle symbolic names) to the types registered from that source.
     */
    private final Map<String, Set<Class<? extends Thing>>> typesBySource =
            new ConcurrentHashMap<>();

    /**
     * Value factory for creating IRIs from strings.
     */
    private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> Optional<TypeMetadata<T>> getMetadata(Class<T> type) {
        return Optional.ofNullable((TypeMetadata<T>) typesByClass.get(type));
    }

    @Override
    public Optional<TypeMetadata<? extends Thing>> getMetadata(IRI typeIRI) {
        return Optional.ofNullable(typesByIRI.get(typeIRI));
    }

    @Override
    public Optional<TypeMetadata<? extends Thing>> getMetadata(String typeIRI) {
        return getMetadata(VALUE_FACTORY.createIRI(typeIRI));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> List<TypeMetadata<? extends T>> getMetadataForType(Class<T> type) {
        return typesByClass.values().stream()
                .filter(metadata -> type.isAssignableFrom(metadata.getType()))
                .map(metadata -> (TypeMetadata<? extends T>) metadata)
                .collect(Collectors.toList());
    }

    @Override
    public List<TypeMetadata<? extends Thing>> getMetadataForType(IRI typeIRI) {
        return typesByClass.values().stream()
                .filter(metadata -> metadata.isOrExtendsType(typeIRI))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> List<TypeMetadata<? extends T>> getSortedMetadataForType(Class<T> type) {
        return typesByClass.values().stream()
                .filter(metadata -> type.isAssignableFrom(metadata.getType()))
                .sorted(Comparator.comparingInt((TypeMetadata<?> m) -> m.getHierarchyDepth()).reversed())
                .map(metadata -> (TypeMetadata<? extends T>) metadata)
                .collect(Collectors.toList());
    }

    @Override
    public List<TypeMetadata<? extends Thing>> getSortedMetadataForType(IRI typeIRI) {
        return typesByClass.values().stream()
                .filter(metadata -> metadata.isOrExtendsType(typeIRI))
                .sorted(Comparator.comparingInt((TypeMetadata<?> m) -> m.getHierarchyDepth()).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public <T extends Thing> void register(TypeMetadata<T> metadata) {
        register(metadata, null);
    }

    @Override
    public <T extends Thing> void register(TypeMetadata<T> metadata, String source) {
        typesByClass.put(metadata.getType(), metadata);
        typesByIRI.put(metadata.getTypeIRI(), metadata);

        if (source != null) {
            typesBySource.computeIfAbsent(source, k -> ConcurrentHashMap.newKeySet())
                    .add(metadata.getType());
        }
    }

    @Override
    public <T extends Thing> boolean unregister(Class<T> type) {
        TypeMetadata<? extends Thing> removed = typesByClass.remove(type);
        if (removed != null) {
            typesByIRI.remove(removed.getTypeIRI());
            // Remove from source tracking
            typesBySource.values().forEach(types -> types.remove(type));
            return true;
        }
        return false;
    }

    @Override
    public void unregisterBySource(String source) {
        Set<Class<? extends Thing>> types = typesBySource.remove(source);
        if (types != null) {
            for (Class<? extends Thing> type : types) {
                TypeMetadata<? extends Thing> metadata = typesByClass.remove(type);
                if (metadata != null) {
                    typesByIRI.remove(metadata.getTypeIRI());
                }
            }
        }
    }

    @Override
    public Set<IRI> getRegisteredTypeIRIs() {
        return Collections.unmodifiableSet(new HashSet<>(typesByIRI.keySet()));
    }

    @Override
    public Set<Class<? extends Thing>> getRegisteredTypes() {
        return Collections.unmodifiableSet(new HashSet<>(typesByClass.keySet()));
    }

    @Override
    public boolean isRegistered(Class<? extends Thing> type) {
        return typesByClass.containsKey(type);
    }

    @Override
    public boolean isRegistered(IRI typeIRI) {
        return typesByIRI.containsKey(typeIRI);
    }

    /**
     * @return The number of registered types
     */
    public int size() {
        return typesByClass.size();
    }

    /**
     * Clears all registered types from this registry.
     */
    public void clear() {
        typesByClass.clear();
        typesByIRI.clear();
        typesBySource.clear();
    }
}
