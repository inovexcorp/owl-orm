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
import com.realmone.owl.orm.types.ValueConverterRegistry;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This is a base implementation of the {@link Thing} interface that will allow generalized proxying of "things" defined
 * in your OWL ontologies.
 */
public class BaseThing implements Thing {

    /**
     * The {@link ValueFactory} for working with RDF {@link Value} data.
     */
    protected static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    /**
     * The {@link Resource} identifier for this {@link Thing}.
     */
    @Getter
    @NonNull
    protected final Resource resource;

    /**
     * The backing {@link Model} for this {@link Thing} entity.
     */
    @Getter
    @NonNull
    protected final Model model;

    @Getter
    @NonNull
    protected final Set<IRI> parents;

    /**
     * The IRI of the type of thing we're working with.
     */
    @Getter
    protected final IRI typeIri;

    /**
     * The {@link ValueConverterRegistry} to convert between types.
     */
    @NonNull
    protected final ValueConverterRegistry valueConverterRegistry;

    @Getter(AccessLevel.PACKAGE)
    protected boolean detached = false;

    @Builder(setterPrefix = "use")
    protected BaseThing(@NonNull Resource resource, @NonNull Model model, @NonNull IRI typeIri, Set<IRI> parents,
                        @NonNull ValueConverterRegistry registry, boolean create) {
        this.resource = resource;
        this.model = model;
        this.parents = parents != null ? parents : new HashSet<>();
        this.typeIri = typeIri;
        this.valueConverterRegistry = registry;
        if (create) {
            // Ensure the type statement(s) exist in the model. If they already exist, model.add() is a no-op.
            this.model.add(resource, RDF.TYPE, typeIri);
            this.parents.forEach(parent -> this.model.add(resource, RDF.TYPE, parent));
        } else {
            // For get/existing mode: check if the resource has any statements in the model.
            // This is lenient about type triples (doesn't require rdf:type) but still requires
            // the resource to exist in the model as a subject.
            if (model.filter(resource, null, null).isEmpty()) {
                detached = true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Value> getProperty(@NonNull IRI predicate, IRI... context) {
        return getProperties(predicate, context).stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Value> getProperties(@NonNull final IRI predicate, @NonNull final IRI... context) {
        return model.filter(resource, predicate, null, context).objects();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setProperty(@NonNull Value value, @NonNull IRI predicate, IRI... context) {
        // Remove other properties with same prediciate...
        model.remove(getResource(), predicate, null, context);
        return model.add(getResource(), predicate, value, context);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(@NonNull Set<Value> values, @NonNull IRI predicate, IRI... context) {
        // Remove other properties with same predicate...
        model.remove(getResource(), predicate, null, context);
        values.forEach(value -> model.add(getResource(), predicate, value, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addProperty(@NonNull Value value, @NonNull IRI predicate, IRI... context) {
        return model.add(getResource(), predicate, value, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeProperty(@NonNull Value value, @NonNull IRI predicate, IRI... context) {
        return model.remove(resource, predicate, value, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clearProperty(@NonNull IRI predicate, IRI... context) {
        return model.remove(resource, predicate, null, context);
    }

    /**
     * Two BaseThing instances are considered equal if they share the same resource IRI.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Thing other) {
            return resource.equals(other.getResource());
        }
        return false;
    }

    /**
     * The hashCode is based solely on the resource IRI for consistency with equals.
     */
    @Override
    public int hashCode() {
        return resource.hashCode();
    }
}
