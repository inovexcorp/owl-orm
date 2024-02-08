package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.annotations.Type;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;
import java.util.Set;

@Type("urn://example#ExampleClass")
public interface ExampleClass extends Thing {

    /*
        Functional datatype property.
     */
    @Property(value = "urn://name", functional = true, type = String.class)
    Optional<String> getName();

    @Property(value = "urn://name", functional = true, type = String.class)
    void setName(String value);

    /*
        Non-functional datatype property.
     */
    @Property(value = "urn://list", type = String.class)
    Set<String> getList();

    @Property(value = "urn://list", type = String.class)
    void setList(Set<String> list);

    @Property(value = "urn://list", type = String.class)
    boolean addToList(String value);

    @Property(value = "urn://list", type = String.class)
    boolean removeFromList(String value);

    /*
        Functional object property...
     */
    @Property(value = "urn://points.to", type = ExampleClass.class, functional = true)
    Optional<ExampleClass> getPointsTo();

    @Property(value = "urn://points.to", type = ExampleClass.class, functional = true)
    Resource getPointsTo_Resource();

    @Property(value = "urn://points.to", type = ExampleClass.class, functional = true)
    void setPointsTo(ExampleClass exampleClass);

    @Property(value = "urn://points.to", type = ExampleClass.class, functional = true)
    void setPointsTo_Resource(Resource resource);

    /*
        Non-functional object property...
     */
    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    Set<ExampleClass> getMultiPointsTo();

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    void setMultiPointsTo(Set<ExampleClass> data);

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    void setMultiPointsToResource(Set<Resource> data);

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    boolean addMultiPointsTo(ExampleClass exampleClass);

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    boolean removeMultiPointsTo(ExampleClass exampleClass);
}
