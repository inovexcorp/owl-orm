package com.realmone.owl.orm.types;

import java.util.Optional;

/**
 * This service provider for all the registered {@link ValueConverter}s.
 *
 * @author bdgould
 */
@FunctionalInterface
public interface ValueConverterRegistry {

    /**
     * Get a registered {@link ValueConverter} instance.
     *
     * @param type The type of {@link ValueConverter} you want
     * @return The {@link ValueConverter} of the specified type if it exists
     */
    <T> Optional<ValueConverter<T>> getValueConverter(Class<T> type);
}
