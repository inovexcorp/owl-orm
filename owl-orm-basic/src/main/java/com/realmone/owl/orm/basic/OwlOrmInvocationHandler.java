package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.types.ValueConversionException;
import com.realmone.owl.orm.types.ValueConverter;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
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
    private final ValueConverterRegistry valueConverterRegistry;
    private final ThingFactory thingFactory;
    private final Model model;
    private final BaseThing delegate;

    public OwlOrmInvocationHandler(Resource resource, Class<? extends Thing> type, Model model,
                                   ValueConverterRegistry valueConverterRegistry, ThingFactory factory) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);
        if (typeAnn == null) {
            throw new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName());
        }
        this.valueConverterRegistry = valueConverterRegistry;
        this.thingFactory = factory;
        this.model = model;
        this.delegate = BaseThing.builder()
                .useCreate(false)
                .useModel(model)
                .useResource(resource)
                .useTypeIri(VALUE_FACTORY.createIRI(typeAnn.value()))
                .useValueConverterRegistry(valueConverterRegistry)
                .build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("toString")) {
            return this.toString();
        } else {
            Optional<Object> opt = useDelegateMethod(proxy, method, args);
            return opt.orElseGet(() -> intercept(proxy, method, args));
        }
    }

    private Optional<Object> useDelegateMethod(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass().equals(Thing.class) || method.getDeclaringClass().equals(Object.class)) {
            try {
                return Optional.of(method.invoke(delegate, args));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Issue reflecting method call to delegate underlying Thing", e);
            }
        } else {
            return Optional.empty();
        }
    }

    private Object intercept(Object proxy, Method method, Object[] args) throws ValueConversionException {
        final Property propertyAnn = method.getDeclaredAnnotation(Property.class);
        final IRI predicate = iri(propertyAnn.value());
        final Class<?> type = propertyAnn.type();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("ORM Property lookup intercepted: {}\n\t{}", method.getName(), propertyAnn);
        }
        if (method.getName().startsWith("get")) {
            if (propertyAnn.functional()) {
                return getFunctionalPropertyValue(predicate, type);
            } else {
                return getNonFunctionalPropertyValue(predicate, type);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "OwlOrmInvocationHandler{" +
                "resource=" + delegate.resource.stringValue() +
                " type=" + delegate.getTypeIri().stringValue() + '}';
    }

    private IRI iri(String value) {
        return VALUE_FACTORY.createIRI(value);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getFunctionalPropertyValue(IRI predicate, Class<T> type) {
        if (Thing.class.isAssignableFrom(type)) {
            ValueConverter<IRI> iriConverter = valueConverterRegistry.getValueConverter(IRI.class)
                    .orElseThrow(() -> new IllegalArgumentException("Couldn't find Converter for IRIs"));
            return (Optional<T>) delegate.getProperty(predicate).map(iriConverter::convertValue).flatMap(iri ->
                    thingFactory.get((Class<? extends Thing>) type, iri, delegate.model, valueConverterRegistry));
        } else {
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
                    .map(iri -> thingFactory.get((Class<? extends Thing>) type, iri, model, valueConverterRegistry)
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
}
