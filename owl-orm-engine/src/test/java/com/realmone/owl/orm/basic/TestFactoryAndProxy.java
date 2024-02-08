package com.realmone.owl.orm.basic;


import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.types.DefaultValueConverterRegistry;
import com.realmone.owl.orm.types.IRIValueConverter;
import com.realmone.owl.orm.types.StringValueConverter;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TestFactoryAndProxy {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private static final DefaultValueConverterRegistry VALUE_CONVERTER_REGISTRY = new DefaultValueConverterRegistry();

    private static final ThingFactory THING_FACTORY = new BaseThingFactory(VALUE_CONVERTER_REGISTRY);

    @BeforeClass
    public static void initRegistry() {
        VALUE_CONVERTER_REGISTRY.register(String.class, new StringValueConverter());
        VALUE_CONVERTER_REGISTRY.register(IRI.class, new IRIValueConverter());
    }

    private Model model;

    @Before
    public void initModel() throws Exception {
        try (Reader reader = new FileReader("src/test/resources/testData.ttl")) {
            this.model = Rio.parse(reader, RDFFormat.TURTLE);
        }
    }

    @Test
    public void testGetFunctionalDatatype() throws Exception {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        Assert.assertEquals("Simple Property Value",
                myThing.getProperty(VALUE_FACTORY.createIRI("urn://name")).orElseThrow().stringValue());
        Assert.assertEquals("OwlOrmInvocationHandler {resource=urn://one type=urn://example#ExampleClass}",
                myThing.toString());
        Assert.assertEquals("Simple Property Value", myThing.getName().orElseThrow());
    }

    @Test
    public void testGetNonfunctionalDatatype() throws Exception {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
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

    @Test
    public void testGetFunctionalObject() throws Exception {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        IRI otherThing = (IRI) myThing.getProperty(iri("urn://points.to")).orElseThrow();
        ExampleClass pointedTo = myThing.getPointsTo().orElseThrow();
        Assert.assertEquals(otherThing, pointedTo.getResource());
    }

    @Test
    public void testGetNonFunctionalObject() throws Exception {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        Set<IRI> iris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        Set<ExampleClass> remoteThings = myThing.getMultiPointsTo();
        Assert.assertEquals(iris.size(), remoteThings.size());
        remoteThings.stream().map(ExampleClass::getResource).map(resource -> (IRI) resource)
                .forEach(resource -> Assert.assertTrue(iris.contains(resource)));
    }

    @Test
    public void testSetFunctionalDatatype() {
        final String newName = "New Name";
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Assert.assertEquals("Simple Property Value", myThing.getName().orElseThrow());
        myThing.setName(newName);
        Assert.assertEquals("Expected setting of functional property to replace the existing name",
                newName, myThing.getName().orElseThrow());
        Assert.assertEquals("Expected a one-for-one replacement, so model size should be stable",
                sizeBefore, model.size());
    }

    @Test
    public void testSetFunctionalDatatypeToNull() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Assert.assertEquals("Simple Property Value", myThing.getName().orElseThrow());
        myThing.setName(null);
        Assert.assertTrue(myThing.getName().isEmpty());
        Assert.assertEquals("Expected a one-for-one replacement, so model size should be stable",
                sizeBefore - 1, model.size());
    }

    @Test
    public void testSetFunctionObject() {
        IRI updatedPointsTo = VALUE_FACTORY.createIRI("urn://example.updatePointsTo");
        // Start on our thing
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"), model)
                .orElseThrow();
        // Check the model size for stability
        int sizeBefore = model.size();
        // Check the initial otherThing we're pointing to functionally
        IRI otherThing = (IRI) myThing.getProperty(iri("urn://points.to")).orElseThrow();
        // Get the other thing from the functional getter
        ExampleClass pointedTo = myThing.getPointsTo().orElseThrow();
        // Grab the resource
        Resource firstPoint = pointedTo.getResource();
        Assert.assertEquals(otherThing, pointedTo.getResource());
        // Set the functional property by the resource.
        myThing.setPointsTo_Resource(updatedPointsTo);
        // Get the thing and make sure it exists
        ExampleClass secondPointsTo = myThing.getPointsTo().orElseThrow();
        // Ensure
        Assert.assertEquals(updatedPointsTo, secondPointsTo.getResource());
        Assert.assertEquals("Model size should not have changed", sizeBefore, myThing.getModel().size());
        // Set it back to the original thing by passing in the actual ExampleThing object (vs resource).
        myThing.setPointsTo(pointedTo);
        // Ensure it worked
        Assert.assertEquals(firstPoint, myThing.getPointsTo().map(Thing::getResource).orElseThrow());
        Assert.assertEquals("Model size should not have changed", sizeBefore, myThing.getModel().size());
    }

    @Test
    public void testSetFunctionalObjectToNonExist() {
        /*
        Setting a functional property to a resource that doesn't exist in the underlying model should be allowable
        for sophisticated usages, but subsequent gets should not be able to resolve the other thing.
         */
        IRI updatedPointsTo = VALUE_FACTORY.createIRI("urn://does.not.exist");
        // Start on our thing
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"), model)
                .orElseThrow();
        myThing.setPointsTo_Resource(updatedPointsTo);
        Assert.assertTrue("Should not be able to get an ExampleThing for a non-existant resource",
                myThing.getPointsTo().isEmpty());

    }

    @Test
    public void testSetNonFunctionalDatatype() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int beforeModelSize = myThing.getModel().size();
        Set<Value> values = myThing.getProperties(VALUE_FACTORY.createIRI("urn://list"));
        Assert.assertEquals(3, values.size());
        Set<String> newStrings = new HashSet<>(2);
        newStrings.add("First New Thingy");
        newStrings.add("Second new THING");
        myThing.setList(newStrings);
        values = myThing.getProperties(VALUE_FACTORY.createIRI("urn://list"));
        Assert.assertEquals(newStrings.size(), values.size());
        Assert.assertEquals(beforeModelSize - 1, myThing.getModel().size());
    }

    @Test
    public void testSetNonFunctionalDataTypeClear() {
        /*
        Setting a non-functional property to an empty set shoudl clear the list.
         */
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        int listSize = myThing.getList().size();
        myThing.setList(Collections.emptySet());
        Assert.assertTrue("Empty set should have cleared list", myThing.getList().isEmpty());
        Assert.assertEquals(sizeBefore - listSize, model.size());
    }

    private static IRI iri(String value) {
        return VALUE_FACTORY.createIRI(value);
    }
}
