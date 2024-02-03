package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;

import java.util.Optional;
import java.util.Set;

@SuperBuilder(setterPrefix = "use")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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
    protected final IRI typeIri;

    @NonNull
    @Builder.Default
    protected boolean create = false;
    /**
     * The {@link ValueConverterRegistry} to convert between types.
     */
    @NonNull
    protected final ValueConverterRegistry valueConverterRegistry;

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
