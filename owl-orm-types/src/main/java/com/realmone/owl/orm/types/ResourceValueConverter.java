package com.realmone.owl.orm.types;

import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for {@link Resource}.
 *
 * @author bdgould
 */
public class ResourceValueConverter extends AbstractValueConverter<Resource> {

    /**
     * Default constructor.
     */
    public ResourceValueConverter() {
        super(Resource.class);
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * Try and cast the value to a {@link Resource}, and if this doesn't work,
     * then try and create an IRI from the {@link String} value.
     */
    @Override
    public Resource convertValue(@NonNull final Value value)
            throws ValueConversionException {
        if (value instanceof Resource) {
            try {
                return Resource.class.cast(value);
            } catch (ClassCastException e) {
                throw new ValueConversionException("Issue casting value '" + value + "' into a Resource", e);
            }
        } else {
            try {
                return this.valueFactory.createIRI(value.stringValue());
            } catch (Exception e) {
                throw new ValueConversionException("Issue converting '" + value + "' into a IRI object", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Resource type) throws ValueConversionException {
        return type;
    }
}
