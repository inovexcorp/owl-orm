package com.realmone.owl.orm.osgi;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.basic.BaseThingFactory;
import com.realmone.owl.orm.types.impl.BigIntegerValueConverter;
import com.realmone.owl.orm.types.impl.BooleanValueConverter;
import com.realmone.owl.orm.types.impl.CalendarValueConverter;
import com.realmone.owl.orm.types.impl.DateValueConverter;
import com.realmone.owl.orm.types.impl.DefaultValueConverterRegistry;
import com.realmone.owl.orm.types.impl.DoubleValueConverter;
import com.realmone.owl.orm.types.impl.FloatValueConverter;
import com.realmone.owl.orm.types.impl.IRIValueConverter;
import com.realmone.owl.orm.types.impl.IntegerValueConverter;
import com.realmone.owl.orm.types.impl.LiteralValueConverter;
import com.realmone.owl.orm.types.impl.LongValueConverter;
import com.realmone.owl.orm.types.impl.ResourceValueConverter;
import com.realmone.owl.orm.types.impl.ShortValueConverter;
import com.realmone.owl.orm.types.impl.StringValueConverter;
import com.realmone.owl.orm.types.ValueConverter;
import com.realmone.owl.orm.types.impl.ValueValueConverter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component(
        immediate = true,
        service = { ThingFactory.class, OsgiThingFactory.class }
)
public class OsgiThingFactory implements ThingFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiThingFactory.class);
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

    Map<String, ClassLoader> classLoaderMap = new HashMap<>();

    Map<Class<? extends Thing>, Set<Class<? extends Thing>>> parentChildMap = new HashMap<>();

    @Activate
    public void start() {
        LOG.debug("Registering ValueConverters in ValueConverterRegistry");
        converters.forEach(converter -> {
            if (valueConverterRegistry.getValueConverter(converter.getType()).isEmpty()) {
                LOG.trace("Registering converter for {}", converter.getType());
                valueConverterRegistry.register(converter);
            }
        });
    }

    public void addClassLoader(ClassLoader classLoader) {
        String id = String.valueOf(System.identityHashCode(classLoader));
        LOG.debug("Adding ClassLoader {}", id);
        classLoaderMap.putIfAbsent(id, classLoader);
    }

    public void removeClassLoader(ClassLoader classLoader) {
        String id = String.valueOf(System.identityHashCode(classLoader));
        LOG.debug("Removing ClassLoader {}", id);
        classLoaderMap.remove(id);
    }

    public void addClass(IRI classIRI, Class<? extends Thing> clazz) {
        LOG.debug("Registering ORM class {} with IRI {}", clazz.getCanonicalName(), classIRI);
        this.index.putIfAbsent(classIRI, clazz);
        LOG.debug("Updating parent map");
        getParentClasses(clazz).forEach(parent -> {
            LOG.trace("Identified parent {}", parent);
            parentChildMap.computeIfAbsent(parent, k -> new HashSet<>()).add(clazz);
        });
    }

    public void removeClass(IRI classIRI) {
        LOG.debug("Unregistering ORM class IRI {}", classIRI);
        if (this.index.containsKey(classIRI)) {
            Class<? extends Thing> clazz = this.index.get(classIRI);
            getParentClasses(this.index.get(classIRI)).forEach(parent -> {
                if (parentChildMap.containsKey(parent) && parentChildMap.get(parent).contains(clazz)) {
                    LOG.trace("Removing {} as child of {}", clazz, parent);
                    parentChildMap.get(parent).remove(clazz);
                    if (parentChildMap.get(parent).isEmpty()) {
                        LOG.trace("Removing parent from map");
                        parentChildMap.remove(parent);
                    }
                }
            });

            this.index.remove(classIRI);
        }
    }

    public Class<? extends Thing> getClass(IRI classIRI) {
        return this.index.get(classIRI);
    }

    public IRI getIRI(Class<? extends Thing> clazz) {
        return vf.createIRI(getTypeAnnotation(clazz).orElseThrow(() -> new IllegalStateException("Subclass of Thing must have a "
                + "Type annotation")).value());
    }

    public <T extends Thing> List<Class<? extends T>> getChildren(Class<T> clazz) {
        if (parentChildMap.get(clazz) == null) {
            return Collections.emptyList();
        }
        return parentChildMap.get(clazz).stream()
                .filter(clazz::isAssignableFrom)
                .map(childClazz -> (Class<? extends T>) childClazz)
                .sorted((clazz1, clazz2) -> getParentClasses(clazz2).size() - getParentClasses(clazz1).size())
                .collect(Collectors.toList());
    }

    public List<Class<? extends Thing>> getParents(Class<? extends Thing> clazz) {
        return getParentClasses(clazz).stream()
                .filter(parent -> getTypeAnnotation(parent).isPresent())
                .sorted((clazz1, clazz2) -> getParentClasses(clazz2).size() - getParentClasses(clazz1).size())
                .collect(Collectors.toList());
    }

    public <T extends Thing> Optional<Class<? extends T>> getSpecificType(T thing) {
        List<IRI> types = thing.getModel().filter(null, RDF.TYPE, null).stream()
                .map(Statement::getObject)
                .filter(Value::isIRI)
                .map(value -> (IRI) value)
                .toList();
        Set<Class<? extends Thing>> parents = new HashSet<>();

        return types.stream()
                .map(this::getClass)
                .filter(Objects::nonNull)
                .peek(clazz -> parents.addAll(getParentClasses(clazz)))
                .filter(clazz -> !parents.contains(clazz))
                .filter(clazz -> clazz.isAssignableFrom(thing.getClass()))
                .min((clazz1, clazz2) -> getParentClasses(clazz2).size() - getParentClasses(clazz1).size())
                .map(clazz -> (Class<? extends T>) clazz);
    }

    public  <T extends Thing> List<Class<? extends T>> getTypes(Resource id, Model model, Class<T> clazz) {
        List<IRI> types = model.filter(id, RDF.TYPE, null).stream()
                .map(Statement::getObject)
                .filter(Value::isIRI)
                .map(value -> (IRI) value)
                .toList();
        return Stream.concat(this.getChildren(clazz).stream(), Stream.of(clazz))
                .filter(type -> types.contains(this.getIRI(type)))
                .collect(Collectors.toList());
    }

    public <T extends Thing> Optional<Type> getTypeAnnotation(Class<T> type) {
        Type typeAnn = type.getDeclaredAnnotation(Type.class);
        return Optional.ofNullable(typeAnn);
    }

    public <T extends Thing> Set<T> getAll(Class<T> type, Model model) {
        Type annotation = getTypeAnnotation(type).orElseThrow(() ->
                new IllegalStateException("Missing Type annotation on provided Thing subtype: " + type.getName()));
        IRI typeIri = vf.createIRI(annotation.value());
        return model.filter(null, RDF.TYPE, typeIri).stream()
                .map(Statement::getSubject)
                .map(iri -> get(type, iri, model))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @Override
    public <T extends Thing> T create(Class<T> type, Resource resource) throws OrmException {
        return getThingFactory().create(type, resource);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, Resource resource, Model model) throws OrmException {
        return getThingFactory().create(type, resource, model);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, String resource, Model model) throws OrmException {
        return getThingFactory().create(type, resource, model);
    }

    @Override
    public <T extends Thing> T create(Class<T> type, String resource) throws OrmException {
        return getThingFactory().create(type, resource);
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, Resource resource, Model model) throws OrmException {
        return getThingFactory().get(type, resource, model);
    }

    @Override
    public <T extends Thing> Optional<T> get(Class<T> type, String resource, Model model) throws OrmException {
        return getThingFactory().get(type, resource, model);
    }

    @Override
    public ValueFactory getValueFactory() {
        return vf;
    }

    @Override
    public ModelFactory getModelFactory() {
        return mf;
    }

    private ThingFactory getThingFactory() {
        return BaseThingFactory.builder()
                .valueFactory(vf)
                .modelFactory(mf)
                .valueConverterRegistry(valueConverterRegistry)
                .classLoaders(classLoaderMap.values().toArray(new ClassLoader[0]))
                .build();
    }

    private <T extends Thing> Set<Class<? extends Thing>> getParentClasses(Class<T> clazz) {
        if (clazz == Thing.class) {
            return Collections.emptySet();
        }
        return BaseThingFactory.walk(clazz)
                .filter(parent -> parent != null && parent != Thing.class && !parent.equals(clazz) && Thing.class.isAssignableFrom(parent))
                .map(parent -> (Class<? extends Thing>) parent)
                .collect(Collectors.toSet());
    }

    private <T> Stream<T> convertIteratorToStream(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }
}