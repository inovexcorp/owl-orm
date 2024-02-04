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
    @NonNull
    protected final IRI typeIri;

    /**
     * The {@link ValueConverterRegistry} to convert between types.
     */
    @NonNull
    protected final ValueConverterRegistry valueConverterRegistry;

    @Getter(AccessLevel.PACKAGE)
    protected boolean detached = false;

    @Builder(setterPrefix = "use")
    protected BaseThing(Resource resource, Model model, IRI typeIri, ValueConverterRegistry registry, boolean create) {
        this.resource = resource;
        this.model = model;
        this.typeIri = typeIri;
        this.valueConverterRegistry = registry;
        boolean exists = !model.filter(resource, RDF.TYPE, typeIri).isEmpty();
        if (!exists && create) {
            this.model.add(resource, RDF.TYPE, typeIri);
        } else if (exists && create) {
            throw new OrmException("Cannot create an instance of '" + typeIri + "' with resource '"
                    + resource.stringValue() + "' as it already exists in our underlying model");
        } else if (!exists && !create) {
            detached = true;
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
