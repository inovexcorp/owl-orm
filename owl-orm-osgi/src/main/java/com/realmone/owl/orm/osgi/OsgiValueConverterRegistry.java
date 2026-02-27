/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.osgi;

import com.realmone.owl.orm.types.ValueConverterRegistry;
import com.realmone.owl.orm.types.impl.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi service component that provides a {@link ValueConverterRegistry} with all standard
 * value converters pre-registered. This component makes the converter registry available as
 * an OSGi service for injection into other components like {@link OsgiThingFactory}.
 */
@Component(immediate = true, service = ValueConverterRegistry.class)
public class OsgiValueConverterRegistry extends DefaultValueConverterRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiValueConverterRegistry.class);

    @Activate
    void activate() {
        LOG.info("OsgiValueConverterRegistry activating - registering standard converters");
        register(new BigIntegerValueConverter());
        register(new BooleanValueConverter());
        register(new CalendarValueConverter());
        register(new DateValueConverter());
        register(new DoubleValueConverter());
        register(new FloatValueConverter());
        register(new IntegerValueConverter());
        register(new IRIValueConverter());
        register(new LiteralValueConverter());
        register(new LongValueConverter());
        register(new ResourceValueConverter());
        register(new ShortValueConverter());
        register(new StringValueConverter());
        register(new ValueValueConverter());
        LOG.info("OsgiValueConverterRegistry activated with {} converters", 14);
    }
}
