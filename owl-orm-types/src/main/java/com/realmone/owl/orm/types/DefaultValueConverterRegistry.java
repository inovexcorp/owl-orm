/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;


import java.util.*;

/**
 * The default {@link ValueConverterRegistry} instance.
 *
 * @author bdgould
 */
public class DefaultValueConverterRegistry implements ValueConverterRegistry {

    /**
     * {@link Map} of type to boxified type. Boxified meaning if it's a
     * primitive class type, it will return the object wrapper type.
     */
    private static final Map<Class<?>, Class<?>> boxifyMap = new HashMap<>();

    /**
     * Simple static constructor to handle the "boxification" of primitives.
     */
    static {
        boxifyMap.put(boolean.class, Boolean.class);
        boxifyMap.put(byte.class, Byte.class);
        boxifyMap.put(short.class, Short.class);
        boxifyMap.put(char.class, Character.class);
        boxifyMap.put(int.class, Integer.class);
        boxifyMap.put(long.class, Long.class);
        boxifyMap.put(float.class, Float.class);
        boxifyMap.put(double.class, Double.class);
    }

    /**
     * The {@link Map} of registered {@link ValueConverter} objects, organized
     * by their types.
     */
    private final Map<Class<?>, List<ValueConverter<?>>> registry = new HashMap<>();

    /**
     * This method will "boxify" primitives into their {@link Object} type.
     *
     * @param type The type to try and boxify
     * @return The "boxified" type, or the original if it is already not a
     * primitive
     */
    @SuppressWarnings("unchecked")
    protected static <T> Class<T> boxify(final Class<T> type) {
        if (type.isPrimitive() && boxifyMap.containsKey(type)) {
            return (Class<T>) boxifyMap.get(type);
        } else {
            return type;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<ValueConverter<T>> getValueConverter(Class<T> type) {
        return Optional.ofNullable(internalGetValueConverter(type));
    }

    @SuppressWarnings("unchecked")
    public <T> ValueConverter<T> internalGetValueConverter(final Class<T> type) {
        ValueConverter<T> result = null;
        if (registry.containsKey(type)) {
            result = (ValueConverter<T>) registry.get(type).get(0);
        } else {
            // Recurse on directly implemented interfaces
            for (Class<?> clazz : type.getInterfaces()) {
                result = (ValueConverter<T>) internalGetValueConverter(clazz);
                if (result != null) break;
            }
            // Recurse on super class
            if (result == null && type.getSuperclass() != null) {
                result = (ValueConverter<T>) internalGetValueConverter(type.getSuperclass());
            }
        }
        return result;
    }

    public <T> void register(ValueConverter<T> converter) {
        if (this.registry.containsKey(converter.getType())) {
            this.registry.get(converter.getType()).add(converter);
        } else {
            List<ValueConverter<?>> list = new ArrayList<>();
            list.add(converter);
            this.registry.put(converter.getType(), list);
        }
    }
}
