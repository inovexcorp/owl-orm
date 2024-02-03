package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.annotations.Type;

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
    Set<String> setList();

    @Property(value = "urn://list", type = String.class)
    boolean addList(String value);

    @Property(value = "urn://list", type = String.class)
    boolean removeList(String value);

    /*
        Functional object property... TODO
     */
    @Property(value = "urn://points.to", type = ExampleClass.class, functional = true)
    Optional<ExampleClass> getPointsTo();

    /*
        Non-functional object property...
     */
    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    Set<ExampleClass> getMultiPointsTo();

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    void setMultiPointsTo(Set<ExampleClass> data);

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    boolean addMultiPointsTo(ExampleClass exampleClass);

    @Property(value = "urn://multi.points.to", type = ExampleClass.class)
    boolean removeMultiPointsTo(ExampleClass exampleClass);
}
