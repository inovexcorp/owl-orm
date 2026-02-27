/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm;

import org.eclipse.rdf4j.model.IRI;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A registry for {@link TypeMetadata} instances that provides lookup capabilities for OWL ORM types.
 * This registry serves as the central point for type discovery and hierarchy traversal in the ORM framework.
 * <p>
 * In OSGi environments, implementations of this interface should track bundles and automatically
 * register/unregister types as bundles are started and stopped. This allows the type metadata
 * to be available before any services that depend on them are activated, avoiding race conditions.
 * <p>
 * The registry supports several lookup patterns commonly needed when working with RDF data:
 * <ul>
 *   <li>Single type lookup by Java class or IRI</li>
 *   <li>Hierarchy lookups to find all types that extend a base type</li>
 *   <li>Sorted lookups that return types ordered by specificity (most specific first)</li>
 * </ul>
 */
public interface TypeRegistry {

    /**
     * Get the metadata for a specific type by its Java class.
     *
     * @param type The Java interface class to look up
     * @param <T>  The type of Thing
     * @return An Optional containing the metadata if found, or empty if the type is not registered
     */
    <T extends Thing> Optional<TypeMetadata<T>> getMetadata(Class<T> type);

    /**
     * Get the metadata for a specific type by its RDF type IRI.
     *
     * @param typeIRI The RDF type IRI to look up
     * @return An Optional containing the metadata if found, or empty if the type is not registered
     */
    Optional<TypeMetadata<? extends Thing>> getMetadata(IRI typeIRI);

    /**
     * Get the metadata for a specific type by its RDF type IRI string.
     *
     * @param typeIRI The RDF type IRI string to look up
     * @return An Optional containing the metadata if found, or empty if the type is not registered
     */
    Optional<TypeMetadata<? extends Thing>> getMetadata(String typeIRI);

    /**
     * Get all registered type metadata that represent the given type or any of its subtypes.
     * This is useful for finding all factories that could potentially handle a resource
     * with a given base type.
     *
     * @param type The base Java interface class
     * @param <T>  The type of Thing
     * @return A list of all matching type metadata, may be empty but never null
     */
    <T extends Thing> List<TypeMetadata<? extends T>> getMetadataForType(Class<T> type);

    /**
     * Get all registered type metadata that represent the given type IRI or any subtypes of it.
     * This looks for types where the given IRI is either the type's own IRI or is in its
     * parent type IRIs.
     *
     * @param typeIRI The RDF type IRI to match
     * @return A list of all matching type metadata, may be empty but never null
     */
    List<TypeMetadata<? extends Thing>> getMetadataForType(IRI typeIRI);

    /**
     * Get all registered type metadata that represent the given type or any of its subtypes,
     * sorted by hierarchy depth in descending order (most specific types first).
     * <p>
     * This ordering is critical when determining which type should handle a given RDF resource
     * that has multiple rdf:type statements - the most specific type should be tried first.
     *
     * @param type The base Java interface class
     * @param <T>  The type of Thing
     * @return A sorted list of all matching type metadata, most specific first
     */
    <T extends Thing> List<TypeMetadata<? extends T>> getSortedMetadataForType(Class<T> type);

    /**
     * Get all registered type metadata that represent the given type IRI or any subtypes,
     * sorted by hierarchy depth in descending order (most specific types first).
     *
     * @param typeIRI The RDF type IRI to match
     * @return A sorted list of all matching type metadata, most specific first
     */
    List<TypeMetadata<? extends Thing>> getSortedMetadataForType(IRI typeIRI);

    /**
     * Register a type metadata instance with this registry. If metadata for the same
     * type class is already registered, it will be replaced.
     *
     * @param metadata The type metadata to register
     * @param <T>      The type of Thing
     */
    <T extends Thing> void register(TypeMetadata<T> metadata);

    /**
     * Unregister a type metadata instance from this registry.
     *
     * @param type The Java interface class to unregister
     * @param <T>  The type of Thing
     * @return true if the type was registered and has been removed, false if it was not registered
     */
    <T extends Thing> boolean unregister(Class<T> type);

    /**
     * Unregister all type metadata associated with a specific source identifier.
     * In OSGi environments, this is typically called when a bundle is stopped,
     * using the bundle's symbolic name or ID as the source.
     *
     * @param source The source identifier (e.g., bundle symbolic name)
     */
    void unregisterBySource(String source);

    /**
     * Register a type metadata instance with this registry, associating it with a source.
     * The source identifier can be used later to unregister all types from that source
     * (e.g., when an OSGi bundle is stopped).
     *
     * @param metadata The type metadata to register
     * @param source   The source identifier (e.g., bundle symbolic name)
     * @param <T>      The type of Thing
     */
    <T extends Thing> void register(TypeMetadata<T> metadata, String source);

    /**
     * @return A set of all registered type IRIs
     */
    Set<IRI> getRegisteredTypeIRIs();

    /**
     * @return A set of all registered Java type classes
     */
    Set<Class<? extends Thing>> getRegisteredTypes();

    /**
     * Check if a type is registered.
     *
     * @param type The Java interface class to check
     * @return true if the type is registered
     */
    boolean isRegistered(Class<? extends Thing> type);

    /**
     * Check if a type IRI is registered.
     *
     * @param typeIRI The RDF type IRI to check
     * @return true if a type with this IRI is registered
     */
    boolean isRegistered(IRI typeIRI);
}
