package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.basic.types.DefaultValueConverterRegistry;
import com.realmone.owl.orm.basic.types.StringValueConverter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.util.Set;

public class TestBaseThingFactory {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private static final DefaultValueConverterRegistry VALUE_CONVERTER_REGISTRY = new DefaultValueConverterRegistry();

    @BeforeClass
    public static void initRegistry() {
        VALUE_CONVERTER_REGISTRY.register(String.class, new StringValueConverter());
    }

    private BaseThingFactory factory = new BaseThingFactory();

    private Model model;

    @Before
    public void initModel() throws Exception {
        try (Reader reader = new FileReader("src/test/resources/testData.ttl")) {
            this.model = Rio.parse(reader, RDFFormat.TURTLE);
        }
    }

    @Test
    public void testReadFunctional() throws Exception {
        ExampleClass myThing = factory.create(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model, VALUE_CONVERTER_REGISTRY);
        Assert.assertEquals("Simple Property Value",
                myThing.getProperty(VALUE_FACTORY.createIRI("urn://name")).orElseThrow().stringValue());
        Assert.assertEquals("OwlOrmInvocationHandler{resource=urn://one type=urn://example#ExampleClass}",
                myThing.toString());
        Assert.assertEquals("Simple Property Value", myThing.getName().orElseThrow());
    }

    @Test
    public void testReadNonfunctional() throws Exception {
        ExampleClass myThing = factory.create(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model, VALUE_CONVERTER_REGISTRY);
        Set<Value> values = myThing.getProperties(VALUE_FACTORY.createIRI("urn://list"));
        Assert.assertEquals(3, values.size());
        Set<String> data = myThing.getList();
        Assert.assertEquals(3, data.size());
        Assert.assertTrue(data.contains("One"));
        Assert.assertTrue(data.contains("Two"));
        Assert.assertTrue(data.contains("Three"));
        values.stream().map(Value::stringValue)
                .forEach(value -> Assert.assertTrue("Expected set values to be contained in method results",
                        data.contains(value)));
    }
}
