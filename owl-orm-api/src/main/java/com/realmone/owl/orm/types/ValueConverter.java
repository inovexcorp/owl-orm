package com.realmone.owl.orm.types;

import com.realmone.owl.orm.Thing;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

public interface ValueConverter<T> {

    /**
     * Convert a value to the specified type.
     *
     * @param value The {@link Value} to convert
     * @return The converted instance
     * @throws ValueConversionException If there is an issue converting the value
     */
    T convertValue(@NonNull Value value) throws ValueConversionException;

    /**
     * Convert an instance of the TYPE of object this {@link ValueConverter} works with back into a {@link Value}.
     *
     * @param type The object to convert into a {@link Value}
     * @return The {@link Value} form of the object passed in
     * @throws ValueConversionException If there is an issue performing the conversion
     */
    Value convertType(@NonNull T type) throws ValueConversionException;

    /**
     * @return The type of class this convert works with
     */
    Class<T> getType();
}
