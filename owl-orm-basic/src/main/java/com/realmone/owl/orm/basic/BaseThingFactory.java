package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import java.lang.reflect.Proxy;
import java.util.Optional;

public class BaseThingFactory implements ThingFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T create(Class<T> type, Resource resource,
                                      Model model, ValueConverterRegistry valueConverterRegistry) {
        OwlOrmInvocationHandler handler = new OwlOrmInvocationHandler(resource, type, model, valueConverterRegistry, this);
        // TODO - only if doesn't exist...
        return (T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model, ValueConverterRegistry valueConverterRegistry) {
        OwlOrmInvocationHandler handler = new OwlOrmInvocationHandler(resource, type, model, valueConverterRegistry, this);
        //TODO - only if exists
        return Optional.of((T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler));
    }
}
