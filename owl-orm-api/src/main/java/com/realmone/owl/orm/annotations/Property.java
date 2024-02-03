package com.realmone.owl.orm.annotations;

import org.eclipse.rdf4j.model.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    String value();

    boolean functional() default false;

    Class<?> type() default Value.class;
}
