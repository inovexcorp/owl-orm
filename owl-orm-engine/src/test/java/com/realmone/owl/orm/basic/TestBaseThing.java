/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.types.DefaultValueConverterRegistry;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestBaseThing {

    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();
    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();
    private static final DefaultValueConverterRegistry VALUE_CONVERTER_REGISTRY = new DefaultValueConverterRegistry();
    private static final IRI TYPE_IRI = VALUE_FACTORY.createIRI("urn://type");

    @BeforeClass
    public static void initValueConverterRegistry() {
        //TODO - populate registry?
    }

    @Test
    public void simpleTestBasicThing() {
        Resource thingResource = VALUE_FACTORY.createIRI("urn://basic.thing");
        BaseThing thing = BaseThing.builder()
                .useModel(MODEL_FACTORY.createEmptyModel())
                .useResource(thingResource)
                .useRegistry(VALUE_CONVERTER_REGISTRY)
                .useTypeIri(TYPE_IRI)
                .useCreate(true)
                .build();
        String testNameValue = "TestBasicThing";
        Assert.assertTrue(thing.addProperty(VALUE_FACTORY.createLiteral(testNameValue),
                VALUE_FACTORY.createIRI("urn://test.name")));
        Value val = thing.getProperty(VALUE_FACTORY.createIRI("urn://test.name"))
                .orElseThrow(() -> new IllegalStateException("Expected the test property to exist"));
        Assert.assertNotNull(val);
        Assert.assertEquals(testNameValue, val.stringValue());
        Assert.assertEquals("Expected a single statement with our thing, and a type (2 stmts total)",
                2, thing.getModel().size());
        Assert.assertEquals(thingResource, thing.getResource());
        Assert.assertEquals(0, thing.getParents().size());
    }

    @Test
    public void testBasicThingProperties() {
        BaseThing thing = BaseThing.builder()
                .useModel(MODEL_FACTORY.createEmptyModel())
                .useResource(VALUE_FACTORY.createIRI("urn://basic.thing"))
                .useRegistry(VALUE_CONVERTER_REGISTRY)
                .useCreate(true)
                .useTypeIri(TYPE_IRI)
                .build();
        // Add a list of string values with an arbitrary predicate.
        IRI propertyIri = VALUE_FACTORY.createIRI("urn://list.property");
        List<String> strings = new ArrayList<>(Arrays.asList("One", "Two", "Three", "Magic"));
        thing.setProperties(strings.stream().map(VALUE_FACTORY::createLiteral).collect(Collectors.toSet()),
                propertyIri);
        // Get the list of values in the thing with that arbitrary predicate.
        List<String> values = thing.getProperties(propertyIri).stream().map(Value::stringValue).toList();
        Assert.assertEquals(strings.size(), values.size());
        strings.forEach(input -> Assert.assertTrue("Output values must contain all values\n\tMissing>: "
                + input, values.contains(input)));
        Assert.assertEquals("Expected one statement per input string with our thing",
                strings.size() + 1, thing.getModel().size());
        // Remove one of the strings from the input list and from the associated thing as well.
        String removed = strings.remove(0);
        Assert.assertTrue(thing.removeProperty(VALUE_FACTORY.createLiteral(removed), propertyIri));
        List<String> afterRemovedValues = thing.getProperties(propertyIri).stream().map(Value::stringValue).toList();
        Assert.assertEquals(strings.size(), afterRemovedValues.size());
        strings.forEach(input -> Assert.assertTrue("Output values must contain all values\n\tMissing>: "
                + input, afterRemovedValues.contains(input)));
        Assert.assertEquals("Expected one statement per input string with our thing (and a type stmt)",
                strings.size() + 1, thing.getModel().size());
    }

    @Test
    public void testMultipleThingsOneModel() {
        Model model = MODEL_FACTORY.createEmptyModel();
        IRI thingOneIri = VALUE_FACTORY.createIRI("urn://basic.thing/1");
        IRI thingTwoIri = VALUE_FACTORY.createIRI("urn://basic.thing/2");
        IRI predicate = VALUE_FACTORY.createIRI("urn://test.name");
        BaseThing thing1 = BaseThing.builder()
                .useModel(model)
                .useResource(thingOneIri)
                .useTypeIri(TYPE_IRI)
                .useRegistry(VALUE_CONVERTER_REGISTRY)
                .useCreate(true)
                .build();
        Assert.assertTrue(thing1.addProperty(VALUE_FACTORY.createLiteral("one"), predicate));
        BaseThing thing2 = BaseThing.builder()
                .useModel(model)
                .useTypeIri(TYPE_IRI)
                .useResource(thingTwoIri)
                .useRegistry(VALUE_CONVERTER_REGISTRY)
                .useCreate(true)
                .build();
        Assert.assertTrue(thing2.addProperty(VALUE_FACTORY.createLiteral("two"), predicate));
        Assert.assertEquals(model, thing1.getModel());
        Assert.assertEquals(model, thing2.getModel());
        // Two properties and Two TYPE statements.
        Assert.assertEquals(4, model.size());
    }

    @Test(expected = NullPointerException.class)
    public void badInitialize() {
        BaseThing.builder()
                //.useRegistry() // Skipping to trigger our exception from lombok.
                .useTypeIri(VALUE_FACTORY.createIRI("urn://type"))
                .useCreate(true)
                .useResource(VALUE_FACTORY.createIRI("urn://fake"))
                .useModel(MODEL_FACTORY.createEmptyModel())
                .build();
    }
}
