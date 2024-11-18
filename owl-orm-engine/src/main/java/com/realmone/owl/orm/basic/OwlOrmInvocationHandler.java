/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.types.ValueConversionException;
import com.realmone.owl.orm.types.ValueConverter;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import lombok.Builder;
import lombok.NonNull;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OwlOrmInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OwlOrmInvocationHandler.class);
    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    private static final String SET_PREFIX = "set";
    private static final String ADDTO_PREFIX = "addTo";
    private static final String REMOVEFROM_PREFIX = "removeFrom";
    private static final String CLEAROUT_PREFIX = "clearOut";

    private final Model model;
    private final ThingFactory thingFactory;
    private final BaseThing delegate;
    private final ValueConverterRegistry valueConverterRegistry;

    @Builder(setterPrefix = "use")
    protected OwlOrmInvocationHandler(Model model, BaseThing delegate,
                                      ValueConverterRegistry valueConverterRegistry, ThingFactory factory) {
        this.model = model;
        this.delegate = delegate;
        this.valueConverterRegistry = valueConverterRegistry;
        this.thingFactory = factory;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The base logic that intercepts method calls against proxied instances of {@link Thing} extensions.  This allows
     * our underlying engine to work without implementations of our OWL ORM "thing" interfaces.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("toString")) {
            return this.toString();
        } else {
            Optional<Object> opt = useDelegateMethod(method, args);
            return opt.orElseGet(() -> intercept(method, args));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This toString implementation allows us to represent the resource and type of the thing we're working with.
     */
    @Override
    public String toString() {
        return "OwlOrmInvocationHandler {" +
                "resource=" + delegate.resource.stringValue() +
                " type=" + delegate.getTypeIri().stringValue() + "}";
    }

    /**
     * Determine whether to use a method directly on the delegated {@link BaseThing} class.  If not, then
     * we'll spit back an empty {@link Optional}, indicating that the method should be proxied and layered on top
     * of the {@link BaseThing} functionality.
     *
     * @param method The {@link Method} being called on our proxied object.
     * @param args   The arguments passed into the method invokation.
     * @return The {@link Optional} wrapping the method we should delegate to in our {@link BaseThing} implementation
     * or empty if we should intercept and handle it in our invocation handler.
     */
    private Optional<Object> useDelegateMethod(Method method, Object[] args) {
        if (method.getDeclaringClass().equals(Thing.class) || method.getDeclaringClass().equals(Object.class)) {
            try {
                return Optional.of(method.invoke(delegate, args));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Issue reflecting method call to delegate underlying Thing", e);
            }
        } else {
            // Should be intercepted and handled specifically per this object.
            return Optional.empty();
        }
    }

    /**
     * This method controls the abstraction layer that handles method calls <b>not</b> directly delegated to the
     * underlying {@link BaseThing} implementation.
     *
     * @param method The {@link Method} being called on our proxy class
     * @param methodArgs   The arguments passed into the method
     * @return The {@link Object} that should be returned by the method invocation.
     * @throws ValueConversionException If there is an issue converting values during execution of the proxied
     *                                  method
     */
    private Object intercept(Method method, Object[] methodArgs) throws ValueConversionException {
        final Property propertyAnn = method.getDeclaredAnnotation(Property.class);
        final IRI predicate = iri(propertyAnn.value());
        final Class<?> type = propertyAnn.type();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("ORM Property lookup intercepted: {}\n\t{}", method.getName(), propertyAnn);
        }
        // If we're intercepting a normal OWL ORM accessor method.
        if (isNormalAccessor(method, methodArgs)) {
            return propertyAnn.functional() ? getFunctionalPropertyValue(predicate, type)
                    : getNonFunctionalPropertyValue(predicate, type);
        }
        // Else if we're intercepting a normal OWL ORM modifier method.
        else if (isNormalModifier(method, methodArgs)) {
            if (propertyAnn.functional()) {
                setFunctionalPropertyValue(predicate, type, methodArgs);
            } else {
                setNonFunctionalPropertyValue(predicate, type, methodArgs);
            }
            // Normal modifiers are of void return types.
            return null;
        }
        // Else if we're working on a non-functional add/remove/clear method.
        else if (isNonFunctionalAddRemove(method, methodArgs)) {
            return interceptNonFunctionalModifier(type, propertyAnn, predicate, method, methodArgs);
        }
        // Else it is unclear what type of method we're intercepting... raise an exception!
        else {
            throw new OrmException("Issue proxying unexpected method call: " + method.getName()
                    + "\n\tOn type: " + this);
        }
    }

    private boolean interceptNonFunctionalModifier(Class<?> type, @NonNull Property propertyAnn, IRI predicate, Method method, Object[] args) throws ValueConversionException {
        // Raise exception if we're operating on top of a functional property...
        if (propertyAnn.functional()) {
            throw new OrmException("Cannot overlay an add/remove method on a functional property\n\t"
                    + method.getName() + " - " + this);
        }
        // Else we're truly operating on a non-functional property.
        else {
            if (method.getName().startsWith(CLEAROUT_PREFIX)) {
                return this.delegate.clearProperty(predicate);
            } else {
                if (method.getName().startsWith(ADDTO_PREFIX)) {
                    return add(type, args[0], predicate);
                } else {
                    return remove(type, args[0], predicate);
                }
            }
        }
    }

    private <T> boolean add(Class<T> type, Object parameter, IRI predicate) throws OrmException {
        if (parameter != null) {
            // Object property
            if (Thing.class.isAssignableFrom(type)) {
                final Thing paramThing = ((Thing) parameter);
                final boolean modified = delegate.addProperty(paramThing.getResource(), predicate);
                model.addAll(paramThing.getModel());
                return modified;
            }
            // Datatype Property
            else {
                try {
                    Value value = getRequiredValueConverter(type).convertType(type.cast(parameter));
                    return delegate.addProperty(value, predicate);
                } catch (ClassCastException e) {
                    throw new OrmException(String.format("Issue adding/removing property '%s' from object " +
                            "of type: %s", predicate, this), e);
                }
            }
        } else {
            throw new OrmException("Null value cannot be added or removed from a non-functional property");
        }
    }

    private <T> boolean remove(Class<T> type, Object parameter, IRI predicate) throws OrmException {
        if (parameter != null) {
            // Object property
            if (Thing.class.isAssignableFrom(type)) {
                final Thing paramThing = ((Thing) parameter);
                return delegate.removeProperty(paramThing.getResource(), predicate);
            }
            // Datatype Property
            else {
                try {
                    Value value = getRequiredValueConverter(type).convertType(type.cast(parameter));
                    return delegate.removeProperty(value, predicate);
                } catch (ClassCastException e) {
                    throw new OrmException(String.format("Issue removing property '%s' from object " +
                            "of type: %s", predicate, this), e);
                }
            }
        } else {
            throw new OrmException("Null value cannot be removed from a non-functional property");
        }
    }


    private boolean isNormalAccessor(Method m, Object[] args) {
        //TODO - validate assumptions...
        String name = m.getName();
        return (name.startsWith(GET_PREFIX) || name.startsWith(IS_PREFIX))
                && argsEmpty(args);
    }

    private boolean isNormalModifier(Method m, Object[] args) {
        //TODO - validate assumptions...
        String name = m.getName();
        return name.startsWith(SET_PREFIX) && !argsEmpty(args);
    }

    private boolean isNonFunctionalAddRemove(Method m, Object[] args) {
        //TODO - validate assumptions...
        String name = m.getName();
        return ((name.startsWith(ADDTO_PREFIX) || name.startsWith(REMOVEFROM_PREFIX))
                && !argsEmpty(args)) || (name.startsWith(CLEAROUT_PREFIX) && argsEmpty(args));
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getFunctionalPropertyValue(IRI predicate, Class<T> type) {
        // If it's an object property; and the destination type is a Thing implementation.
        if (Thing.class.isAssignableFrom(type)) {
            // TODO - maybe pull this converter into a field?
            return (Optional<T>) delegate.getProperty(predicate)
                    .map(getRequiredValueConverter(IRI.class)::convertValue)
                    .flatMap(iri -> thingFactory.get((Class<? extends Thing>) type, iri, delegate.getModel()));
        }
        // Else it's a datatype property, look for the appropriate converter.
        else {
            return delegate.getProperty(predicate).map(getRequiredValueConverter(type)::convertValue);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> getNonFunctionalPropertyValue(IRI predicate, Class<T> type) {
        if (Thing.class.isAssignableFrom(type)) {
            return (Set<T>) delegate.getProperties(predicate).stream()
                    .map(getRequiredValueConverter(IRI.class)::convertValue)
                    .map(iri -> thingFactory.get((Class<? extends Thing>) type, iri, delegate.getModel())
                            .orElseThrow(() -> new OrmException("Couldn't get thing for IRI in underlying model: "
                                    + iri)))
                    .collect(Collectors.toSet());
        } else {
            return delegate.getProperties(predicate).stream()
                    .map(getRequiredValueConverter(type)::convertValue).collect(Collectors.toSet());
        }
    }

    private <T> void setFunctionalPropertyValue(IRI predicate, Class<T> type, Object[] args) {
        // If we're dealing with an object property and the target type is a Thing subclass.
        if (Thing.class.isAssignableFrom(type)) {
            // If the arg is a resource, then we'll set it directly
            if (args[0] instanceof Resource resource) {
                delegate.setProperty(resource, predicate);
            }
            // Otherwise if the arg is a Thing, and should go through the delegate layer.
            else if (Thing.class.isAssignableFrom(args[0].getClass())) {
                Thing value = (Thing) args[0];
                delegate.getModel().addAll(value.getModel().filter(value.getResource(), null, null));
                delegate.setProperty(value.getResource(), predicate);
            }
            // Else we don't know how to handle the argument that was passed in!
            else {
                throw new OrmException(String.format("Cannot set a functional property value on '%s' to a " +
                        "type (%s): %s", this, args[0].getClass().getName(), args[0]));
            }
        }
        // Else it's a datatype property.
        else {
            T value = type.cast(args[0]);
            // If the value is null, we should clear the property.
            if (value == null) {
                delegate.clearProperty(predicate);
            }
            // Else try and convert the value and set the property in the delegate.
            else {
                delegate.setProperty(getRequiredValueConverter(type).convertType(value), predicate);
            }
        }
    }

    private <T> void setNonFunctionalPropertyValue(IRI predicate, Class<T> type, Object[] args) {
        if (args[0] instanceof Set<?> setArgument) {
            // If the set is empty...
            if (setArgument.isEmpty()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Empty set passed into '{}' for object: {}", predicate.stringValue(), this);
                }
                this.delegate.clearProperty(predicate);
            }
            // Else the set has stuff in it!
            else {
                // Our method is working with an Object Property
                if (Thing.class.isAssignableFrom(type)) {
                    handleSettingNonfunctionalObjectProperty(setArgument, predicate, args[0]);
                }
                // Else we're working with a data type property.
                else {
                    Set<Value> data = setArgument.stream()
                            .map(entry -> cast(type, entry))
                            .map(getRequiredValueConverter(type)::convertType)
                            .collect(Collectors.toSet());
                    delegate.setProperties(data, predicate);
                }
            }
        }
        // Else it's a non-set type, so we should raise an exception.
        else {
            // Null parameters will land here too...
            throw new OrmException("Setting non functional property value requires usage of a java.util.Set argument");
        }
    }

    private void handleSettingNonfunctionalObjectProperty(Set<?> setArgument, IRI predicate, Object arg) {
        final Object sample = setArgument.stream().findFirst()
                .orElseThrow(() -> new OrmException("Unexpected issue, couldn't get elements of a set when " +
                        "setting a non-functional object property for sampling"));
        // If the arg is a resource, then we'll set it directly
        if (sample instanceof Resource) {
            // Convert the incoming set to a set of resources
            delegate.setProperties(setArgument.stream().map(entry -> cast(Resource.class, entry))
                            .collect(Collectors.toSet()),
                    predicate);
        }
        // Otherwise if the arg is a Thing, and should go through the delegate layer.
        else if (Thing.class.isAssignableFrom(sample.getClass())) {
            delegate.setProperties(setArgument.stream()
                            .map(entry -> cast(Thing.class, entry))
                            .map(thing -> {
                                // Add all the things models to our delegate model
                                delegate.getModel().addAll(thing.getModel());
                                // Map to a resource for the thing
                                return thing.getResource();
                            }).collect(Collectors.toSet()),
                    predicate);
        }
        // Else we don't know how to handle the argument that was passed in!
        else {
            throw new OrmException(String.format("Cannot set a functional property value on '%s' to a " +
                    "type (%s): %s", this, arg.getClass().getName(), arg));
        }
    }

    private <T> ValueConverter<T> getRequiredValueConverter(Class<T> type) {
        return valueConverterRegistry.getValueConverter(type)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find Value Converter for type: "
                        + type.getName()));
    }

    private static <T> T cast(Class<T> type, Object obj) {
        try {
            return type.cast(obj);
        } catch (ClassCastException e) {
            throw new OrmException("Issue casting an object into a specific type for ValueConversion in ORM", e);
        }
    }

    private static IRI iri(String value) {
        return VALUE_FACTORY.createIRI(value);
    }

    private static boolean argsEmpty(Object[] args) {
        return args == null || args.length == 0;
    }
}
