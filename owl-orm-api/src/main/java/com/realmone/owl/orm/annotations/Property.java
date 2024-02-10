package com.realmone.owl.orm.annotations;

import org.eclipse.rdf4j.model.Value;

import java.lang.annotation.*;

/**
 * This annotation is a hint for the OWL ORM Engine to be able to work with generated proxy instances of the
 * interfaces.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    /**
     * @return The {@link String} representation of the property IRI for this method.
     */
    String value();

    /**
     * @return Whether this method overlays a functional property.
     */
    boolean functional() default false;

    /**
     * @return The Java type that this method will work with as a hint to the
     * {@link com.realmone.owl.orm.types.ValueConverter} system.
     */
    Class<?> type() default Value.class;
}
