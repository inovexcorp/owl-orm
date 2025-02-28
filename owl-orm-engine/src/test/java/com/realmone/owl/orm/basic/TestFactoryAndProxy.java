/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.basic;


import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import com.realmone.owl.orm.types.impl.DefaultValueConverterRegistry;
import com.realmone.owl.orm.types.impl.IRIValueConverter;
import com.realmone.owl.orm.types.impl.ResourceValueConverter;
import com.realmone.owl.orm.types.impl.StringValueConverter;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
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

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private static final DefaultValueConverterRegistry VALUE_CONVERTER_REGISTRY = new DefaultValueConverterRegistry();

    private static final ThingFactory THING_FACTORY = BaseThingFactory.builder()
            .modelFactory(MODEL_FACTORY)
            .valueFactory(VALUE_FACTORY)
            .valueConverterRegistry(VALUE_CONVERTER_REGISTRY)
            .build();


    @BeforeClass
    public static void initRegistry() {
        VALUE_CONVERTER_REGISTRY.register(new StringValueConverter());
        VALUE_CONVERTER_REGISTRY.register(new IRIValueConverter());
        VALUE_CONVERTER_REGISTRY.register(new ResourceValueConverter());
    }

    private Model model;

    @Before
    public void initModel() throws Exception {
        try (Reader reader = new FileReader("src/test/resources/testData.ttl")) {
            this.model = Rio.parse(reader, RDFFormat.TURTLE);
        }
    }

    @Test
    public void testGetFunctionalDatatype() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        Assert.assertEquals("Simple Property Value",
                myThing.getProperty(VALUE_FACTORY.createIRI("urn://name")).orElseThrow().stringValue());
        Assert.assertEquals("OwlOrmInvocationHandler {resource=urn://one type=urn://example#ExampleClass}",
                myThing.toString());
        Assert.assertEquals("Simple Property Value", myThing.getName().orElseThrow());
    }

    @Test
    public void testGetNonfunctionalDatatype() {
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
    public void testAddNonfunctionalDatatype() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        String toAdd = "Another thingy-ma-jig";
        Set<Value> values = myThing.getProperties(VALUE_FACTORY.createIRI("urn://list"));
        Assert.assertEquals(3, values.size());
        Assert.assertTrue("Adding to set should be successful!",
                myThing.addToList(toAdd));
        Assert.assertEquals("Should have added one statement for the addToList",
                sizeBefore + 1, model.size());
        Assert.assertTrue("Data should be returned after adding", myThing.getList().contains(toAdd));
    }

    @Test(expected = OrmException.class)
    public void testAddNonfunctionalDatatypeNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"), model)
                .orElseThrow().addToList(null);
    }

    @Test
    public void testRemoveNonfunctionalDatatype() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        int listSizeBefore = myThing.getList().size();
        Assert.assertFalse("Removing a value that isn't present should return false",
                myThing.removeFromList("SHOULD BE NOT"));
        Assert.assertEquals(sizeBefore, model.size());
        Assert.assertTrue(myThing.removeFromList("One"));
        Assert.assertEquals(sizeBefore - 1, model.size());
        Assert.assertEquals(listSizeBefore - 1, myThing.getList().size());
        Assert.assertFalse(myThing.getList().contains("One"));
    }

    @Test(expected = OrmException.class)
    public void testRemoveNonfunctionalDatatypeNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"), model)
                .orElseThrow().removeFromList(null);
    }


    @Test
    public void testGetFunctionalObject() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        IRI otherThing = (IRI) myThing.getProperty(iri("urn://points.to")).orElseThrow();
        ExampleClass pointedTo = myThing.getPointsTo().orElseThrow();
        Assert.assertEquals(otherThing, pointedTo.getResource());
    }

    @Test
    public void testGetFunctionalObjectResource() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        IRI otherThing = (IRI) myThing.getProperty(iri("urn://points.to")).orElseThrow();
        Resource pointedTo = myThing.getPointsTo_Resource().orElseThrow();
        Assert.assertEquals(otherThing, pointedTo);
    }

    @Test
    public void testGetNonFunctionalObject() {
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

    @Test
    public void testClearOutNonFunctionalDatatype() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        int listSize = myThing.getList().size();
        Assert.assertTrue(myThing.clearOutList());
        Assert.assertTrue("Empty set should have cleared list", myThing.getList().isEmpty());
        Assert.assertFalse(myThing.clearOutList());
        Assert.assertEquals(sizeBefore - listSize, model.size());
    }

    @Test
    public void testSetNonFunctionalObjectByIRI() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Set<IRI> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        Assert.assertFalse("Should have some existing relationships", originalIris.isEmpty());
        Set<Resource> newIris = new HashSet<>();
        newIris.add(iri("urn://random.other/1"));
        newIris.add(iri("urn://random.other/2"));
        myThing.setMultiPointsTo_Resource(newIris);
        Set<ExampleClass> result = myThing.getMultiPointsTo();
        Assert.assertEquals(newIris.size(), result.size());
        newIris.forEach(resource -> Assert.assertTrue(result.stream().map(Thing::getResource)
                .anyMatch(resource::equals)));
        Assert.assertEquals((sizeBefore - originalIris.size() + newIris.size()), model.size());
    }

    @Test
    public void testSetNonFunctionalObjectByObject() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Set<Resource> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        ExampleClass r1 = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://random.other/1"),
                model).orElseThrow();
        ExampleClass r2 = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://random.other/2"),
                model).orElseThrow();
        Set<ExampleClass> others = new HashSet<>();
        others.add(r1);
        others.add(r2);
        myThing.setMultiPointsTo(others);
        Assert.assertEquals((sizeBefore - originalIris.size() + others.size()), model.size());
        Set<ExampleClass> afterSet = myThing.getMultiPointsTo();
        Set<Resource> afterRes = afterSet.stream().map(Thing::getResource).collect(Collectors.toSet());
        Set<Resource> inputRes = others.stream().map(Thing::getResource).collect(Collectors.toSet());
        afterRes.forEach(resource -> Assert.assertTrue(inputRes.contains(resource)));
        others.stream().map(Thing::getResource).forEach(resource -> {
            Assert.assertFalse(originalIris.contains(resource));
        });
    }

    @Test(expected = OrmException.class)
    public void testSetNonFunctionalObjectNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow().setMultiPointsTo(null);
    }

    @Test
    public void testSetNonFunctionalObjectEmpty() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int size = model.size();
        Set<Resource> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        myThing.setMultiPointsTo(Collections.emptySet());
        Assert.assertEquals(size - originalIris.size(), model.size());
    }


    @Test
    public void testSetNonFunctionalObjectWithEmpty() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int size = model.size();
        Set<IRI> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        Assert.assertFalse("Should have some existing relationships", originalIris.isEmpty());
        myThing.setMultiPointsTo(Collections.emptySet());
        Assert.assertTrue(myThing.getMultiPointsTo().isEmpty());
        Assert.assertEquals(size - originalIris.size(), model.size());
    }

    @Test(expected = OrmException.class)
    public void testSetNonFunctionalObjectWithNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                        model).orElseThrow()
                // Set a multiPointsTo to null to trigger exception.
                .setMultiPointsTo(null);
    }

    @Test
    public void testAddNonFunctionalObjectWithThing() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Set<Resource> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        ExampleClass r1 = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://random.other/1"),
                model).orElseThrow();
        Assert.assertTrue(myThing.addToMultiPointsTo(r1));
        Assert.assertEquals(sizeBefore + 1, model.size());
        Assert.assertTrue(myThing.getMultiPointsTo().stream().map(Thing::getResource)
                .anyMatch(r1.getResource()::equals));
    }

    @Test(expected = OrmException.class)
    public void testAddNonFunctionalObjectWithThingNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow().addToMultiPointsTo(null);
    }

    @Test
    public void testAddNonFunctionalObjectWithResource() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Set<Resource> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        Resource r1 = VALUE_FACTORY.createIRI("urn://random.other/1");
        Assert.assertTrue(myThing.addToMultiPointsTo_Resource(r1));
        // Second add will be a noop and return false.
        Assert.assertFalse(myThing.addToMultiPointsTo_Resource(r1));
        Assert.assertEquals(sizeBefore + 1, model.size());
        Assert.assertTrue(myThing.getProperties(iri("urn://multi.points.to")).contains(r1));
        Assert.assertEquals(originalIris.size() + 1, myThing.getMultiPointsTo().size());
    }

    @Test(expected = OrmException.class)
    public void testAddNonFunctionalObjectWithResourceNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow().addToMultiPointsTo_Resource(null);
    }

    @Test
    public void testRemoveNonFunctionalObjectWithThing() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        ExampleClass r1 = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://object.property/1"),
                model).orElseThrow();
        Assert.assertTrue(myThing.removeFromMultiPointsTo(r1));
        Assert.assertEquals(sizeBefore - 1, model.size());
        Assert.assertFalse(myThing.getMultiPointsTo().stream().map(Thing::getResource)
                .anyMatch(r1.getResource()::equals));
    }

    @Test(expected = OrmException.class)
    public void testRemoveNonFunctionalObjectWithThingNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow().removeFromMultiPointsTo(null);
    }

    @Test
    public void testRemoveNonFunctionalObjectWithResource() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Set<Resource> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        Resource r1 = VALUE_FACTORY.createIRI("urn://object.property/1");
        Assert.assertTrue(myThing.getProperties(iri("urn://multi.points.to")).contains(r1));
        Assert.assertTrue(myThing.removeFromMultiPointsTo_Resource(r1));
        // Second add will be a noop and return false.
        Assert.assertFalse(myThing.removeFromMultiPointsTo_Resource(r1));
        Assert.assertEquals(sizeBefore - 1, model.size());
        Assert.assertFalse(myThing.getProperties(iri("urn://multi.points.to")).contains(r1));
        Assert.assertEquals(originalIris.size() - 1, myThing.getMultiPointsTo().size());
    }

    @Test(expected = OrmException.class)
    public void testRemoveNonFunctionalObjectWithResourceNull() {
        THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow().removeFromMultiPointsTo_Resource(null);
    }

    @Test
    public void testClearOutNonFunctionalObject() {
        ExampleClass myThing = THING_FACTORY.get(ExampleClass.class, VALUE_FACTORY.createIRI("urn://one"),
                model).orElseThrow();
        int sizeBefore = model.size();
        Set<Resource> originalIris = myThing.getProperties(iri("urn://multi.points.to")).stream().map(Value::stringValue)
                .map(VALUE_FACTORY::createIRI).collect(Collectors.toSet());
        Assert.assertTrue(myThing.clearOutMultiPointsTo());
        Assert.assertTrue(myThing.getMultiPointsTo().isEmpty());
        Assert.assertEquals(sizeBefore - originalIris.size(), model.size());
        Assert.assertFalse(myThing.clearOutMultiPointsTo());
        Assert.assertEquals(sizeBefore - originalIris.size(), model.size());
    }

    private static IRI iri(String value) {
        return VALUE_FACTORY.createIRI(value);
    }
}
