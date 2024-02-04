package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.lang.reflect.Proxy;
import java.util.Optional;

public class BaseThingFactory implements ThingFactory {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T create(Class<T> type, Resource resource,
                                      Model model, ValueConverterRegistry valueConverterRegistry) {
        Type annotation = getTypeAnnotation(type);
        IRI typeIri = VALUE_FACTORY.createIRI(annotation.value());
        OwlOrmInvocationHandler handler = OwlOrmInvocationHandler.builder()
                .useFactory(this)
                .useValueConverterRegistry(valueConverterRegistry)
                .useModel(model)
                .useDelegate(BaseThing.builder()
                        .useModel(model)
                        .useResource(resource)
                        .useTypeIri(typeIri)
                        .useRegistry(valueConverterRegistry)
                        .useCreate(true)
                        .build())
                .build();
        return (T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model,
                                             ValueConverterRegistry valueConverterRegistry) {
        Type annotation = getTypeAnnotation(type);
        IRI typeIri = VALUE_FACTORY.createIRI(annotation.value());
        BaseThing delegate = BaseThing.builder()
                .useModel(model)
                .useResource(resource)
                .useTypeIri(typeIri)
                .useRegistry(valueConverterRegistry)
                .useCreate(false)
                .build();
        if (delegate.isDetached()) {
            return Optional.empty();
        } else {
            OwlOrmInvocationHandler handler = OwlOrmInvocationHandler.builder()
                    .useFactory(this)
                    .useValueConverterRegistry(valueConverterRegistry)
                    .useModel(model)
                    .useDelegate(delegate)
                    .build();
            return Optional.of((T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                    new Class[]{type}, handler));
        }
    }

    private <T extends Thing> Type getTypeAnnotation(Class<T> type) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);

        if (typeAnn == null) {
            throw new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName());
        }
        return typeAnn;
    }
}
