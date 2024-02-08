package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.annotations.Type;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;
import java.util.Set;

@Type(ExampleClass.TYPE_IRI)
public interface ExampleClass extends Thing {

    String TYPE_IRI = "urn://example#ExampleClass";
    String PREDICATE_NAME = "urn://name";
    String PREDICATE_LIST = "urn://list";
    String PREDICATE_POINTSTO = "urn://points.to";
    String PREDICATE_MULTIPOINTSTO = "urn://multi.points.to";

    /*
        Functional datatype property.
     */
    @Property(value = PREDICATE_NAME, functional = true, type = String.class)
    Optional<String> getName();

    @Property(value = PREDICATE_NAME, functional = true, type = String.class)
    void setName(String value);

    /*
        Non-functional datatype property.
     */
    @Property(value = PREDICATE_LIST, type = String.class)
    Set<String> getList();

    @Property(value = PREDICATE_LIST, type = String.class)
    void setList(Set<String> list);

    @Property(value = PREDICATE_LIST, type = String.class)
    boolean addToList(String value);

    @Property(value = PREDICATE_LIST, type = String.class)
    boolean removeFromList(String value);

    @Property(value = PREDICATE_LIST, type = String.class)
    boolean clearOutList();

    /*
        Functional object property...
     */
    @Property(value = PREDICATE_POINTSTO, type = ExampleClass.class, functional = true)
    Optional<ExampleClass> getPointsTo();

    @Property(value = PREDICATE_POINTSTO, type = Resource.class, functional = true)
    Optional<Resource> getPointsTo_Resource();

    @Property(value = PREDICATE_POINTSTO, type = ExampleClass.class, functional = true)
    void setPointsTo(ExampleClass exampleClass);

    @Property(value = PREDICATE_POINTSTO, type = Resource.class, functional = true)
    void setPointsTo_Resource(Resource resource);

    /*
        Non-functional object property...
     */
    @Property(value = PREDICATE_MULTIPOINTSTO, type = ExampleClass.class)
    Set<ExampleClass> getMultiPointsTo();

    @Property(value = PREDICATE_MULTIPOINTSTO, type = ExampleClass.class)
    void setMultiPointsTo(Set<ExampleClass> data);

    @Property(value = PREDICATE_MULTIPOINTSTO, type = Resource.class)
    void setMultiPointsTo_Resource(Set<Resource> data);

    @Property(value = PREDICATE_MULTIPOINTSTO, type = ExampleClass.class)
    boolean addToMultiPointsTo(ExampleClass exampleClass);

    @Property(value = PREDICATE_MULTIPOINTSTO, type = Resource.class)
    boolean addToMultiPointsTo_Resource(Resource exampleClass);

    @Property(value = PREDICATE_MULTIPOINTSTO, type = ExampleClass.class)
    boolean removeFromMultiPointsTo(ExampleClass exampleClass);

    @Property(value = PREDICATE_MULTIPOINTSTO, type = Resource.class)
    boolean removeFromMultiPointsTo_Resource(Resource exampleClass);

    @Property(value = PREDICATE_MULTIPOINTSTO, type = ExampleClass.class)
    boolean clearOutMultiPointsTo();
}
