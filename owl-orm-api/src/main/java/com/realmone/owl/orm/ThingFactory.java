package com.realmone.owl.orm;

import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;

public interface ThingFactory {

    <T extends Thing> T create(Class<T> type, Resource resource, Model model,
                               ValueConverterRegistry valueConverterRegistry);

    <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model,
                                      ValueConverterRegistry valueConverterRegistry);

}
