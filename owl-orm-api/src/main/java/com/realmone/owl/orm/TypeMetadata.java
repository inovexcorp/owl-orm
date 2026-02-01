/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm;

import org.eclipse.rdf4j.model.IRI;

import java.util.Set;

/**
 * This interface provides metadata about a generated OWL ORM type. It contains all the information
 * needed to work with instances of the type at runtime, including the type hierarchy information
 * that was determined at code generation time from the OWL ontology.
 * <p>
 * TypeMetadata instances are typically generated alongside the Thing interfaces and registered
 * with a {@link TypeRegistry} at bundle startup time. This allows the type hierarchy to be
 * known statically rather than discovered dynamically at runtime, avoiding race conditions
 * in OSGi environments.
 *
 * @param <T> The type of {@link Thing} this metadata describes
 */
public interface TypeMetadata<T extends Thing> {

    /**
     * @return The Java interface class that this metadata describes
     */
    Class<T> getType();

    /**
     * @return The RDF4j {@link IRI} that identifies instances of this type in RDF data
     */
    IRI getTypeIRI();

    /**
     * @return The string representation of the type IRI
     */
    default String getTypeIRIString() {
        return getTypeIRI().stringValue();
    }

    /**
     * Returns the set of parent type IRIs for this type. This includes all ancestor types
     * in the OWL class hierarchy, computed at code generation time from rdfs:subClassOf
     * relationships in the ontology.
     * <p>
     * This set does NOT include the type's own IRI - only its ancestors.
     *
     * @return The set of parent type {@link IRI}s, never null but may be empty for root types
     */
    Set<IRI> getParentTypeIRIs();

    /**
     * Returns the depth of this type in the inheritance hierarchy. Root types (those with
     * no parents other than owl:Thing) have depth 0. Each level of inheritance adds 1.
     * <p>
     * This is useful for sorting types from most specific (highest depth) to most general
     * (lowest depth) when determining which factory should handle a given RDF resource.
     *
     * @return The hierarchy depth, where 0 indicates a root type
     */
    default int getHierarchyDepth() {
        return getParentTypeIRIs().size();
    }

    /**
     * Checks whether this type is the same as or a subtype of the given type IRI.
     * This is useful for filtering types that can handle a given RDF type.
     *
     * @param typeIRI The type IRI to check against
     * @return true if this type's IRI equals the given IRI or if the given IRI is in this type's parent IRIs
     */
    default boolean isOrExtendsType(IRI typeIRI) {
        return getTypeIRI().equals(typeIRI) || getParentTypeIRIs().contains(typeIRI);
    }

    /**
     * Checks whether this type is the same as or a subtype of the given Java type.
     *
     * @param type The Java class to check against
     * @return true if this metadata's type is assignable to the given class
     */
    default boolean isOrExtendsType(Class<? extends Thing> type) {
        return type.isAssignableFrom(getType());
    }
}
