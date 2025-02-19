package com.realmone.owl.orm.osgi;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.basic.BaseThingFactory;
import com.realmone.owl.orm.types.BigIntegerValueConverter;
import com.realmone.owl.orm.types.BooleanValueConverter;
import com.realmone.owl.orm.types.CalendarValueConverter;
import com.realmone.owl.orm.types.DateValueConverter;
import com.realmone.owl.orm.types.DefaultValueConverterRegistry;
import com.realmone.owl.orm.types.DoubleValueConverter;
import com.realmone.owl.orm.types.FloatValueConverter;
import com.realmone.owl.orm.types.IRIValueConverter;
import com.realmone.owl.orm.types.IntegerValueConverter;
import com.realmone.owl.orm.types.LiteralValueConverter;
import com.realmone.owl.orm.types.LongValueConverter;
import com.realmone.owl.orm.types.ResourceValueConverter;
import com.realmone.owl.orm.types.ShortValueConverter;
import com.realmone.owl.orm.types.StringValueConverter;
import com.realmone.owl.orm.types.ValueConverter;
import com.realmone.owl.orm.types.ValueValueConverter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component(
        immediate = true,
        service = ThingFactoryProvider.class
)
public class ThingFactoryProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ThingFactoryProvider.class);
    private static final DefaultValueConverterRegistry valueConverterRegistry = new DefaultValueConverterRegistry();
    private static final ModelFactory mf = new DynamicModelFactory();
    private static final ValueFactory vf = new ValidatingValueFactory();

    private static final List<ValueConverter<?>> converters = List.of(
            new BigIntegerValueConverter(),
            new BooleanValueConverter(),
            new CalendarValueConverter(),
            new DateValueConverter(),
            new DoubleValueConverter(),
            new FloatValueConverter(),
            new IntegerValueConverter(),
            new IRIValueConverter(),
            new LiteralValueConverter(),
            new LongValueConverter(),
            new ResourceValueConverter(),
            new ShortValueConverter(),
            new StringValueConverter(),
            new ValueValueConverter()
    );

    Map<IRI, Class<? extends Thing>> index = new HashMap<>();

    // Approach #1
//    Map<Class<? extends Thing>, Set<Class<? extends Class<? extends Thing>>>> parentChildMap = new HashMap<>();
    // Approach #2
//    Map<Class<? super Class<? extends Thing>>, Set<Class<? extends Thing>>> parentChildMap = new HashMap<>();
    // Approach #3
//    ClassHierarchy<Thing> hierarchy;

    // Approach #4
//    Set<Parent<? extends Thing>> parents = new HashSet<>();

    @Activate
    protected void start() {
        LOG.debug("Registering ValueConverters in ValueConverterRegistry");
        converters.forEach(converter -> {
            if (valueConverterRegistry.getValueConverter(converter.getType()).isEmpty()) {
                LOG.trace("Registering converter for {}", converter.getType());
                valueConverterRegistry.register(converter);
            }
        });
        // Approach #3
//        this.hierarchy = new ClassHierarchy<>();
    }


    public void addClass(IRI classIRI, Class<? extends Thing> clazz) {
        LOG.debug("Registering ORM class {} with IRI {}", clazz.getCanonicalName(), classIRI);
        this.index.putIfAbsent(classIRI, clazz);
        LOG.debug("Updating parent map");
        // Approach #3
//        hierarchy.populateClassHierarchy(clazz);
        // Approach #4
//        populateParents(clazz);
        // Approach #1 and 2
//        getParents(clazz).forEach(parent -> {
//            LOG.trace("Identified parent {}", parent);
//            parentChildMap.computeIfAbsent(parent, k -> new HashSet<>()).add(clazz);
//        });
    }

    public void removeClass(IRI classIRI) {
        LOG.debug("Unregistering ORM class IRI {}", classIRI);
        if (this.index.containsKey(classIRI)) {
            // Approach #1 and 2
//            Class<? extends Thing> clazz = this.index.get(classIRI);
//            getParents(this.index.get(classIRI)).forEach(parent -> {
//                if (parentChildMap.containsKey(parent) && parentChildMap.get(parent).contains(clazz)) {
//                    LOG.trace("Removing {} as child of {}", clazz, parent);
//                    parentChildMap.get(parent).remove(clazz);
//                    if (parentChildMap.get(parent).isEmpty()) {
//                        LOG.trace("Removing parent from map");
//                        parentChildMap.remove(parent);
//                    }
//                }
//            });
            // Didn't get to this with the other approachs...

            this.index.remove(classIRI);
        }
    }

    public Class<? extends Thing> getClass(IRI classIRI) {
        return this.index.get(classIRI);
    }

    public <T extends Thing> Set<? extends T> getChildren(Class<T> clazz) {
        return new HashSet<>();
        // Approach #3
//        return hierarchy.getSubclasses(clazz);
    }

    // Approach #1 and #2
//    @SuppressWarnings("unchecked")
//    public <T extends Thing> Set<Class<? extends T>> getChildren(Class<T> clazz) {
//        return (Set<Class<? extends T>>) (Set<?>) this.parentChildMap.getOrDefault(clazz, new HashSet<>());
//    }

    public ThingFactory getThingFactory() {
        return BaseThingFactory.builder()
                .valueFactory(vf)
                .modelFactory(mf)
                .valueConverterRegistry(valueConverterRegistry)
                .build();
    }

    public <T extends Thing> Optional<Type> getTypeAnnotation(Class<T> type) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);
        return Optional.ofNullable(typeAnn);
    }

    // Approach #4
//    private <T extends Thing> void populateParents(Class<T> clazz) {
//        if (clazz != Thing.class) {
//            Class<? super T> parentClass = clazz.getSuperclass();
//            if (parentClass != null && parentClass != Thing.class && Thing.class.isAssignableFrom(parentClass)) {
//                Optional<Parent<? extends Thing>> optParent = parents.stream().filter(parent -> parent.getType() == parentClass).findFirst();
//                if (optParent.isPresent()) {
//                    optParent.get().children.add(clazz);
//                } else {
//                    Parent<? extends Parent> newParent = new Parent(parentClass);
//                    newParent.children.add(clazz);
//                    parents.add(newParent);
//                }
//            }
//        }
//    }

    // Approach #1
//    private <T extends Thing> Set<Class<? extends Thing>> getParents(Class<T> clazz) {
//        if (clazz == Thing.class) {
//            return Collections.emptySet();
//        }
//        Set<Class<? extends Thing>> parents = new HashSet<>();
//        Class<? super T> parent = clazz.getSuperclass();
//        while (parent != null && parent != Thing.class && Thing.class.isAssignableFrom(parent)) {
//            parents.add((Class<? extends Thing>) parent);
//            parent = parent.getSuperclass();
//        }
//        return parents;
//    }

    // Approach #2
//    private <T extends Thing> Set<Class<? super T>> getParents(Class<T> clazz) {
//        if (clazz == Thing.class) {
//            return Collections.emptySet();
//        }
//        Set<Class<? super T>> parents = new HashSet<>();
//        Class<? super T> parent = clazz.getSuperclass();
//        while (parent != null && parent != Thing.class && Thing.class.isAssignableFrom(parent)) {
//            parents.add(parent);
//            parent = parent.getSuperclass();
//        }
//        return parents;
//    }

    // Another option for Approach #1 instead of getParents
//    private <T extends Thing> void populateClassHierarchy(Class<T> clazz) {
//        Class<? super T> superClass = clazz.getSuperclass();
//        if (superClass != null && superClass != Thing.class && Thing.class.isAssignableFrom(superClass)) {
//            Class<? extends Thing> parent = (Class<? extends Thing>) superClass;
//            addClass(parent, clazz);
//            populateClassHierarchy(parent);
//        }
//    }
//    private <T extends Thing> void addClass(Class<T> parentClass, Class<? extends T> subClass) {
//        parentChildMap.computeIfAbsent(parentClass, k -> new HashSet<>()).add(subClass);
//    }

    // Approach #3
//    private class ClassHierarchy<T extends Thing> {
//        private Map<Class<? extends T>, Set<Class<? extends T>>> parentChildMap = new HashMap<>();
//
//        public void addClass(Class<? extends T> parentClass, Class<? extends T> subClass) {
//            parentChildMap.computeIfAbsent(parentClass, k -> new HashSet<>()).add(subClass);
//        }
//
//        public Set<Class<? extends T>> getSubclasses(Class<? extends T> parentClass) {
//            return parentChildMap.getOrDefault(parentClass, new HashSet<>());
//        }
//
//        public void populateClassHierarchy(Class<? extends T> clazz) {
//            Class<? extends T> superClass = (Class<? extends T>) clazz.getSuperclass();
//            if (superClass != null && Thing.class.isAssignableFrom(superClass)) {
//                addClass(superClass, clazz);
//                populateClassHierarchy(superClass);
//            }
//        }
//    }

    // Approach #4
//    private class Parent<T extends Thing> {
//        public Set<Class<? extends T>> children = new HashSet<>();
//
//        Class<T> type;
//
//        Parent(Class<T> clazz) {
//            this.type = clazz;
//        }
//
//        Class<T> getType() {
//            return type;
//        }
//    }
}
