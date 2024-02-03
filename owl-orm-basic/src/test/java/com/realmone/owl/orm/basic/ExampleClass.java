package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.annotations.Property;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.annotations.Type;

import java.util.Optional;
import java.util.Set;

@Type("urn://example#ExampleClass")
public interface ExampleClass extends Thing {

    @Property(value = "urn://name", functional = true, type = String.class)
    Optional<String> getName();

    @Property(value = "urn://name", functional = true, type = String.class)
    void setName(String value);

    @Property(value = "urn://list", type = String.class)
    Set<String> getList();

    @Property(value = "urn://list", type = String.class)
    Set<String> setList();

    @Property(value = "urn://list", type = String.class)
    boolean addList(String value);

    @Property(value = "urn://list", type = String.class)
    boolean removeList(String value);


}
