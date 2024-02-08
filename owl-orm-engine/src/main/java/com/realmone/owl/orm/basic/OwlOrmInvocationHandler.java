package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.types.ValueConversionException;
import com.realmone.owl.orm.types.ValueConverter;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import lombok.Builder;
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
     * @param args   The arguments passed into the method
     * @return The {@link Object} that should be returned by the method invocation.
     * @throws ValueConversionException If there is an issue converting values during execution of the proxied
     *                                  method
     */
    private Object intercept(Method method, Object[] args) throws ValueConversionException {
        final Property propertyAnn = method.getDeclaredAnnotation(Property.class);
        final IRI predicate = iri(propertyAnn.value());
        final Class<?> type = propertyAnn.type();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("ORM Property lookup intercepted: {}\n\t{}", method.getName(), propertyAnn);
        }
        // If we're intercepting a normal OWL ORM accessor method.
        if (isNormalAccessor(method, args)) {
            return propertyAnn.functional() ? getFunctionalPropertyValue(predicate, type)
                    : getNonFunctionalPropertyValue(predicate, type);
        }
        // Else if we're intercepting a normal OWL ORM modifier method.
        else if (isNormalModifier(method, args)) {
            if (propertyAnn.functional()) {
                setFunctionalPropertyValue(predicate, type, args);
            } else {
                //TODO
                setNonFunctionalPropertyValue(predicate, type, args);
            }
            // Normal modifiers are of void return types.
            return null;
        }
        // Else if we're working on a non-functional add/remove method.
        else if (isNonFunctionalAddRemove(method, args)) {
            // Raise exception if we're operating on top of a functional property...
            if (propertyAnn.functional()) {
                throw new OrmException("Cannot overlay an add/remove method on a functional property\n\t"
                        + method.getName() + " - " + this);
            } else {
                //TODO
            }
        }
        // Else it is unclear what type of method we're intercepting... raise an exception!
        else {
            throw new OrmException("Issue proxying unexpected method call: " + method.getName()
                    + "\n\tOn type: " + this);
        }
        return null;
    }

    private boolean isNormalAccessor(Method m, Object[] args) {
        //TODO - validate assumptions...
        String name = m.getName();
        return (name.startsWith("get") || name.startsWith("is"))
                && argsEmpty(args);
    }

    private boolean isNormalModifier(Method m, Object[] args) {
        //TODO - validate assumptions...
        String name = m.getName();
        return name.startsWith("set") && !argsEmpty(args);
    }

    private boolean isNonFunctionalAddRemove(Method m, Object[] args) {
        String name = m.getName();
        return (name.startsWith("add") || name.startsWith("remove")) && !argsEmpty(args);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getFunctionalPropertyValue(IRI predicate, Class<T> type) {
        // If it's an object property; and the destination type is a Thing implementation.
        if (Thing.class.isAssignableFrom(type)) {
            ValueConverter<IRI> iriConverter = valueConverterRegistry.getValueConverter(IRI.class)
                    .orElseThrow(() -> new IllegalArgumentException("Couldn't find Converter for IRIs"));
            return (Optional<T>) delegate.getProperty(predicate).map(iriConverter::convertValue).flatMap(iri ->
                    thingFactory.get((Class<? extends Thing>) type, iri, delegate.getModel()));
        }
        // Else it's a datatype property, look for the appropriate converter.
        else {
            ValueConverter<T> converter = valueConverterRegistry.getValueConverter(type)
                    .orElseThrow(() -> new IllegalArgumentException("Couldn't find Value Converter for type: "
                            + type.getName()));
            return delegate.getProperty(predicate).map(converter::convertValue);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> getNonFunctionalPropertyValue(IRI predicate, Class<T> type) {
        if (Thing.class.isAssignableFrom(type)) {
            ValueConverter<IRI> converter = valueConverterRegistry.getValueConverter(IRI.class)
                    .orElseThrow(() -> new IllegalArgumentException("Couldn't find Converter for IRIs"));
            return (Set<T>) delegate.getProperties(predicate).stream().map(converter::convertValue)
                    .map(iri -> thingFactory.get((Class<? extends Thing>) type, iri, delegate.getModel())
                            .orElseThrow(() -> new OrmException("Couldn't get thing for IRI in underlying model: "
                                    + iri)))
                    .collect(Collectors.toSet());
        } else {
            final ValueConverter<T> converter = valueConverterRegistry.getValueConverter(type)
                    .orElseThrow(() -> new IllegalArgumentException("Couldn't find Value Converter for type: "
                            + type.getName()));
            return delegate.getProperties(predicate).stream()
                    .map(converter::convertValue).collect(Collectors.toSet());
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
            if (value == null) {
                delegate.clearProperty(predicate);
            } else {
                ValueConverter<T> converter = valueConverterRegistry.getValueConverter(type)
                        .orElseThrow(() -> new IllegalArgumentException("Couldn't find Value Converter for type: "
                                + type.getName()));
                delegate.setProperty(converter.convertType(value), predicate);
            }
        }
    }

    private <T> void setNonFunctionalPropertyValue(IRI predicate, Class<T> type, Object[] args) {
        if (args[0] instanceof Set<?> setArgument) {
            if (setArgument.isEmpty()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Empty set passed into '{}' for object: {}", predicate.stringValue(), this);
                }
                this.delegate.clearProperty(predicate);
            } else {
                // Our method is working with an Object Property
                if (Thing.class.isAssignableFrom(type)) {

                    //TODO - handle setting non-functional Object Properties.

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
                // Else we're working with a data type property.
                else {
                    ValueConverter<T> converter = valueConverterRegistry.getValueConverter(type)
                            .orElseThrow(() -> new IllegalArgumentException("Couldn't find Value Converter for type: "
                                    + type.getName()));
                    Set<Value> data = setArgument.stream()
                            .map(entry -> cast(type, entry))
                            .map(converter::convertType)
                            .collect(Collectors.toSet());
                    delegate.setProperties(data, predicate);
                }
            }


        } else {
            throw new OrmException("Setting non functional property value requires usage of a java.util.Set argument");
        }
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
