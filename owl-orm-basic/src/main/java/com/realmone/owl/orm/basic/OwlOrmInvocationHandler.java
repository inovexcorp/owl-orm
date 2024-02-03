package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.types.ValueConversionException;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class OwlOrmInvocationHandler implements InvocationHandler {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();
    private final ValueConverterRegistry valueConverterRegistry;
    private final BaseThing delegate;

    public OwlOrmInvocationHandler(Resource resource, Class<? extends Thing> type, Model model,
                                   ValueConverterRegistry valueConverterRegistry) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);
        if (typeAnn == null) {
            throw new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName());
        }
        this.valueConverterRegistry = valueConverterRegistry;
        this.delegate = BaseThing.builder()
                .useCreate(false)
                .useModel(model)
                .useResource(resource)
                .useTypeIri(VALUE_FACTORY.createIRI(typeAnn.value()))
                .useValueConverterRegistry(valueConverterRegistry)
                .build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("toString")) {
            return this.toString();
        } else {
            Optional<Object> opt = useDelegateMethod(proxy, method, args);
            return opt.orElseGet(() -> intercept(proxy, method, args));
        }
    }

    private Optional<Object> useDelegateMethod(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass().equals(Thing.class)) {
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
//        System.out.printf("ORM Property: %s%n\t%s%n", propertyAnn, method);
        if (method.getName().startsWith("get")) {
            if (propertyAnn.functional()) {
                return getFunctionalProperty(predicate, type);
            } else {

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

    private Optional<Object> getFunctionalProperty(IRI predicate, Class<?> type) {
        Value value = delegate.getProperty(predicate).orElse(null);
        Object obj = null;
        if (value != null) {
            obj = valueConverterRegistry.getValueConverter(type)
                    .orElseThrow(() -> new IllegalArgumentException("Couldn't find Value Converter for type: " + type.getName()))
                    .convertValue(value);
        }
        return Optional.ofNullable(obj);
    }
}
