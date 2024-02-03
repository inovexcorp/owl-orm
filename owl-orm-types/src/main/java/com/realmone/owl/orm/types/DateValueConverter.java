package com.realmone.owl.orm.types;

import com.realmone.owl.orm.types.ValueConversionException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for creating {@link java.util.Date} objects from statements.
 *
 * @author bdgould
 */
public class DateValueConverter extends AbstractValueConverter<OffsetDateTime> {

    /**
     * Default constructor.
     */
    public DateValueConverter() {
        super(OffsetDateTime.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OffsetDateTime convertValue(@NonNull final Value value) throws ValueConversionException {
        try {
            return OffsetDateTime.parse(value.stringValue(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ValueConversionException("Issue converting value of statement into an OffsetDateTime object.", e);
        }
    }

    @Override
    public Value convertType(@NonNull OffsetDateTime type) throws ValueConversionException {
        try {
            return this.valueFactory.createLiteral(type);
        } catch (Exception e) {
            throw new ValueConversionException("Issue converting calendar into Value", e);
        }
    }
}
