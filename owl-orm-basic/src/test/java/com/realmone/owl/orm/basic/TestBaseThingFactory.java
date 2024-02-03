package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.basic.types.DefaultValueConverterRegistry;
import com.realmone.owl.orm.basic.types.StringValueConverter;
import com.realmone.owl.orm.types.ValueConverterRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;

public class TestBaseThingFactory {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private static final DefaultValueConverterRegistry VALUE_CONVERTER_REGISTRY = new DefaultValueConverterRegistry();

    @BeforeClass
    public static void initRegistry() {
        VALUE_CONVERTER_REGISTRY.register(String.class, new StringValueConverter());
    }

    @Test
    public void simpleTest() throws Exception {
        Model model = readTestData();
        BaseThingFactory factory = new BaseThingFactory();
        ExampleClass myThing = factory.create(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model, VALUE_CONVERTER_REGISTRY);
        Assert.assertEquals("Simple Property Value",
                myThing.getProperty(VALUE_FACTORY.createIRI("urn://name")).orElseThrow().stringValue());
        Assert.assertEquals("OwlOrmInvocationHandler{resource=urn://one type=urn://example#ExampleClass}",
                myThing.toString());
        Assert.assertEquals("Simple Property Value", myThing.getName().orElseThrow());


        System.out.println(myThing.getName());
        myThing.setName("NEW");
        System.out.println(myThing.getList());
    }

    private Model readTestData() throws Exception {
        try (Reader reader = new FileReader("src/test/resources/testData.ttl")) {
            return Rio.parse(reader, RDFFormat.TURTLE);
        }
    }
}
