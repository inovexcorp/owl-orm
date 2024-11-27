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
import com.realmone.owl.orm.types.ValueConverter;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Optional;
import java.util.Set;

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
    protected BaseThing(@NonNull Resource resource, @NonNull Model model, @NonNull IRI typeIri,
                        @NonNull ValueConverterRegistry registry, boolean create) {
        this.resource = resource;
        this.model = model;
        this.typeIri = typeIri;
        this.valueConverterRegistry = registry;
        boolean exists = !model.filter(resource, RDF.TYPE, typeIri).isEmpty();
        if (!exists) {
            if (create) { // If it doesn't exist and we are creating, add the statement.
                this.model.add(resource, RDF.TYPE, typeIri);
            } else { // If it doesn't exist and we are not creating, note that we are detached for the facade...
                detached = true;
            }
        } else {
            if (create) { // If it exists and we are creating, raise an exception...
                throw new OrmException("Cannot create an instance of '" + typeIri + "' with resource '"
                        + resource.stringValue() + "' as it already exists in our underlying model");
            }
            // Just get it if it exists!
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
}
