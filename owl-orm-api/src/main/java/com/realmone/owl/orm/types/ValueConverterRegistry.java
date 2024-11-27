/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
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
