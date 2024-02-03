package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import java.lang.reflect.Proxy;

public class BaseThingFactory implements ThingFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T create(Class<T> type, Resource resource,
                                      Model model, ValueConverterRegistry valueConverterRegistry) {
        OwlOrmInvocationHandler handler = new OwlOrmInvocationHandler(resource, type, model, valueConverterRegistry);
        return (T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T get(Class<T> type, Resource resource, Model model, ValueConverterRegistry valueConverterRegistry) {
        OwlOrmInvocationHandler handler = new OwlOrmInvocationHandler(resource, type, model, valueConverterRegistry);
        return (T) Proxy.newProxyInstance(OwlOrmInvocationHandler.class.getClassLoader(),
                new Class[]{type}, handler);
    }
}
