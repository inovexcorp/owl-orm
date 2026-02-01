/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.osgi;

import com.realmone.owl.orm.*;
import com.realmone.owl.orm.basic.BaseThingFactory;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An OSGi service component that provides a {@link ThingFactory} implementation backed by
 * a {@link TypeRegistry}. This factory provides additional methods beyond the standard
 * {@link ThingFactory} interface for working with type hierarchies and RDF type discovery.
 * <p>
 * This component depends on:
 * <ul>
 *   <li>{@link TypeRegistry} - for type metadata lookup</li>
 *   <li>{@link ValueConverterRegistry} - for value type conversions</li>
 * </ul>
 * <p>
 * The factory provides methods that mirror the functionality needed by the Mobi OrmFactoryRegistry:
 * <ul>
 *   <li>{@link #getMetadataForType(Class)} - get all type metadata for a type hierarchy</li>
 *   <li>{@link #getSortedMetadataForType(Class)} - same, but sorted by specificity</li>
 *   <li>{@link #findMostSpecificType(Resource, Model, Class)} - determine the most specific type for a resource</li>
 * </ul>
 */
@Component(immediate = true, service = {ThingFactory.class, OsgiThingFactory.class})
public class OsgiThingFactory implements ThingFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiThingFactory.class);

    @Reference
    private TypeRegistry typeRegistry;

    @Reference
    private ValueConverterRegistry valueConverterRegistry;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final ModelFactory modelFactory = new DynamicModelFactory();

    /**
     * The delegate factory for actual object creation.
     */
    private BaseThingFactory delegate;

    @Activate
    void activate() {
        LOG.info("OsgiThingFactory activating");
        delegate = BaseThingFactory.builder()
                .valueConverterRegistry(valueConverterRegistry)
                .valueFactory(valueFactory)
                .modelFactory(modelFactory)
                .build();
        LOG.info("OsgiThingFactory activated with {} registered types",
                typeRegistry.getRegisteredTypes().size());
    }

    @Deactivate
    void deactivate() {
        LOG.info("OsgiThingFactory deactivating");
        delegate = null;
    }

    // ========================================================================
    // Standard ThingFactory methods (delegated)
    // ========================================================================

    @Override
    public <T extends Thing> T create(Class<T> type, Resource resource) throws OrmException {
        return delegate.create(type, resource);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, Resource resource, Model model) throws OrmException {
        return delegate.create(type, resource, model);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, String resource, Model model) throws OrmException {
        return delegate.create(type, resource, model);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, String resource) throws OrmException {
        return delegate.create(type, resource);
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model) throws OrmException {
        return delegate.get(type, resource, model);
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, String resource, Model model) throws OrmException {
        return delegate.get(type, resource, model);
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    @Override
    public ModelFactory getModelFactory() {
        return modelFactory;
    }

    // ========================================================================
    // TypeRegistry-backed methods (Mobi OrmFactoryRegistry compatibility)
    // ========================================================================

    /**
     * Get the type metadata for a specific type.
     *
     * @param type The Java interface class
     * @param <T>  The type of Thing
     * @return Optional containing the metadata, or empty if not registered
     */
    public <T extends Thing> Optional<TypeMetadata<T>> getMetadata(Class<T> type) {
        return typeRegistry.getMetadata(type);
    }

    /**
     * Get the type metadata for a specific type IRI.
     *
     * @param typeIRI The RDF type IRI
     * @return Optional containing the metadata, or empty if not registered
     */
    public Optional<TypeMetadata<? extends Thing>> getMetadata(IRI typeIRI) {
        return typeRegistry.getMetadata(typeIRI);
    }

    /**
     * Get all type metadata that represent the given type or any of its subtypes.
     * This is equivalent to Mobi's {@code OrmFactoryRegistry.getFactoriesOfType(Class)}.
     *
     * @param type The base Java interface class
     * @param <T>  The type of Thing
     * @return List of all matching type metadata
     */
    public <T extends Thing> List<TypeMetadata<? extends T>> getMetadataForType(Class<T> type) {
        return typeRegistry.getMetadataForType(type);
    }

    /**
     * Get all type metadata that represent the given type IRI or any subtypes.
     * This is equivalent to Mobi's {@code OrmFactoryRegistry.getFactoriesOfType(IRI)}.
     *
     * @param typeIRI The RDF type IRI
     * @return List of all matching type metadata
     */
    public List<TypeMetadata<? extends Thing>> getMetadataForType(IRI typeIRI) {
        return typeRegistry.getMetadataForType(typeIRI);
    }

    /**
     * Get all type metadata sorted by hierarchy depth (most specific first).
     * This is equivalent to Mobi's {@code OrmFactoryRegistry.getSortedFactoriesOfType(Class)}.
     *
     * @param type The base Java interface class
     * @param <T>  The type of Thing
     * @return Sorted list of all matching type metadata, most specific first
     */
    public <T extends Thing> List<TypeMetadata<? extends T>> getSortedMetadataForType(Class<T> type) {
        return typeRegistry.getSortedMetadataForType(type);
    }

    /**
     * Get all type metadata sorted by hierarchy depth (most specific first).
     * This is equivalent to Mobi's {@code OrmFactoryRegistry.getSortedFactoriesOfType(IRI)}.
     *
     * @param typeIRI The RDF type IRI
     * @return Sorted list of all matching type metadata, most specific first
     */
    public List<TypeMetadata<? extends Thing>> getSortedMetadataForType(IRI typeIRI) {
        return typeRegistry.getSortedMetadataForType(typeIRI);
    }

    /**
     * Determines the most specific registered type for a resource based on its rdf:type statements.
     * This examines the rdf:type values in the model and finds the most specific (deepest in hierarchy)
     * registered type that matches.
     * <p>
     * This is a common pattern in Mobi where code needs to determine the actual implementation type
     * of a resource rather than just using the base type.
     *
     * @param resource The resource to examine
     * @param model    The model containing the resource's type statements
     * @param baseType The base type to constrain the search to
     * @param <T>      The base type
     * @return The most specific TypeMetadata matching the resource's types, or empty if none found
     */
    public <T extends Thing> Optional<TypeMetadata<? extends T>> findMostSpecificType(
            Resource resource, Model model, Class<T> baseType) {

        // Get all rdf:type values for this resource
        Set<IRI> rdfTypes = model.filter(resource, RDF.TYPE, null).stream()
                .map(Statement::getObject)
                .filter(IRI.class::isInstance)
                .map(IRI.class::cast)
                .collect(Collectors.toSet());

        if (rdfTypes.isEmpty()) {
            return Optional.empty();
        }

        // Get all registered types that are subtypes of baseType, sorted by depth
        List<TypeMetadata<? extends T>> candidates = getSortedMetadataForType(baseType);

        // Find the first (most specific) candidate whose type IRI is in the resource's rdf:types
        return candidates.stream()
                .filter(metadata -> rdfTypes.contains(metadata.getTypeIRI()))
                .findFirst();
    }

    /**
     * Get an existing Thing from the model, automatically determining the most specific registered type.
     * This combines type discovery with object retrieval.
     *
     * @param resource The resource to retrieve
     * @param model    The model containing the resource
     * @param baseType The base type to constrain the search to
     * @param <T>      The base type
     * @return The Thing instance cast to the most specific type, or empty if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Thing> Optional<T> getExistingMostSpecific(
            Resource resource, Model model, Class<T> baseType) {

        return findMostSpecificType(resource, model, baseType)
                .flatMap(metadata -> {
                    // Cast is safe because findMostSpecificType ensures it's a subtype of baseType
                    Class<? extends T> specificType = (Class<? extends T>) metadata.getType();
                    return get(specificType, resource, model).map(thing -> (T) thing);
                });
    }

    /**
     * Stream all resources in the model that have an rdf:type matching any registered subtype
     * of the given base type.
     *
     * @param model    The model to search
     * @param baseType The base type to look for
     * @param <T>      The base type
     * @return A list of resources that have matching rdf:type statements
     */
    public <T extends Thing> List<Resource> findResourcesOfType(Model model, Class<T> baseType) {
        // Get all type IRIs for the base type and its subtypes
        Set<IRI> typeIRIs = getMetadataForType(baseType).stream()
                .map(TypeMetadata::getTypeIRI)
                .collect(Collectors.toSet());

        // Find all resources with any of these types
        return model.filter(null, RDF.TYPE, null).stream()
                .filter(stmt -> stmt.getObject() instanceof IRI)
                .filter(stmt -> typeIRIs.contains((IRI) stmt.getObject()))
                .map(Statement::getSubject)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get all existing Things of the given type (or subtypes) from the model.
     *
     * @param model    The model to search
     * @param baseType The base type to look for
     * @param <T>      The base type
     * @return A list of Thing instances
     */
    public <T extends Thing> List<T> getAllExisting(Model model, Class<T> baseType) {
        return findResourcesOfType(model, baseType).stream()
                .map(resource -> getExistingMostSpecific(resource, model, baseType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Check if a type is registered in the type registry.
     *
     * @param type The Java interface class to check
     * @return true if registered
     */
    public boolean isTypeRegistered(Class<? extends Thing> type) {
        return typeRegistry.isRegistered(type);
    }

    /**
     * Check if a type IRI is registered in the type registry.
     *
     * @param typeIRI The RDF type IRI to check
     * @return true if registered
     */
    public boolean isTypeRegistered(IRI typeIRI) {
        return typeRegistry.isRegistered(typeIRI);
    }

    /**
     * @return The underlying TypeRegistry
     */
    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }
}
