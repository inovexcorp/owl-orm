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
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;

import java.lang.reflect.Proxy;
import java.util.Optional;

public class BaseThingFactory implements ThingFactory {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private ValueConverterRegistry valueConverterRegistry;

    private final ModelFactory modelFactory;
    private final ValueFactory valueFactory;

    public BaseThingFactory(ValueConverterRegistry valueConverterRegistry,
                            ModelFactory modelFactory, ValueFactory valueFactory) {
        this.valueConverterRegistry = valueConverterRegistry;
        this.modelFactory = modelFactory;
        this.valueFactory = valueFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> T create(Class<T> type, Resource resource, Model model) throws OrmException {
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
    public <T extends Thing> T create(Class<T> type, String resource, Model model) throws OrmException {
        return create(type, valueFactory.createIRI(resource), model);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model) throws OrmException {
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

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    @Override
    public ModelFactory getModelFactory() {
        return modelFactory;
    }

    private <T extends Thing> Type getTypeAnnotation(Class<T> type) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);

        if (typeAnn == null) {
            throw new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName());
        }
        return typeAnn;
    }
}
