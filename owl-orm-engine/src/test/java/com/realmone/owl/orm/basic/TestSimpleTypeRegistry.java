/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.basic;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.TypeMetadata;
import com.realmone.owl.orm.annotations.Type;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SimpleTypeRegistry}.
 */
public class TestSimpleTypeRegistry {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI ANIMAL_IRI = VF.createIRI("http://example.org/Animal");
    private static final IRI DOG_IRI = VF.createIRI("http://example.org/Dog");
    private static final IRI CAT_IRI = VF.createIRI("http://example.org/Cat");
    private static final IRI THING_IRI = VF.createIRI("http://www.w3.org/2002/07/owl#Thing");

    private SimpleTypeRegistry registry;

    // Test interfaces
    @Type("http://example.org/Animal")
    interface Animal extends Thing {}

    @Type("http://example.org/Dog")
    interface Dog extends Animal {}

    @Type("http://example.org/Cat")
    interface Cat extends Animal {}

    @Before
    public void setUp() {
        registry = new SimpleTypeRegistry();
    }

    @Test
    public void testRegisterAndGetByClass() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata);

        Optional<TypeMetadata<Animal>> retrieved = registry.getMetadata(Animal.class);
        assertTrue(retrieved.isPresent());
        assertEquals(Animal.class, retrieved.get().getType());
        assertEquals(ANIMAL_IRI, retrieved.get().getTypeIRI());
    }

    @Test
    public void testRegisterAndGetByIRI() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata);

        Optional<TypeMetadata<? extends Thing>> retrieved = registry.getMetadata(ANIMAL_IRI);
        assertTrue(retrieved.isPresent());
        assertEquals(Animal.class, retrieved.get().getType());
    }

    @Test
    public void testGetByIRIString() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata);

        Optional<TypeMetadata<? extends Thing>> retrieved = registry.getMetadata("http://example.org/Animal");
        assertTrue(retrieved.isPresent());
        assertEquals(Animal.class, retrieved.get().getType());
    }

    @Test
    public void testGetMetadataForTypeReturnsSubtypes() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        TypeMetadata<Dog> dogMetadata = SimpleTypeMetadata.<Dog>builder()
                .type(Dog.class)
                .typeIRI(DOG_IRI)
                .parentTypeIRIs(Set.of(ANIMAL_IRI, THING_IRI))
                .build();

        TypeMetadata<Cat> catMetadata = SimpleTypeMetadata.<Cat>builder()
                .type(Cat.class)
                .typeIRI(CAT_IRI)
                .parentTypeIRIs(Set.of(ANIMAL_IRI, THING_IRI))
                .build();

        registry.register(animalMetadata);
        registry.register(dogMetadata);
        registry.register(catMetadata);

        List<TypeMetadata<? extends Animal>> animals = registry.getMetadataForType(Animal.class);
        assertEquals(3, animals.size());

        List<TypeMetadata<? extends Dog>> dogs = registry.getMetadataForType(Dog.class);
        assertEquals(1, dogs.size());
    }

    @Test
    public void testGetSortedMetadataForTypeSortsByDepth() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        TypeMetadata<Dog> dogMetadata = SimpleTypeMetadata.<Dog>builder()
                .type(Dog.class)
                .typeIRI(DOG_IRI)
                .parentTypeIRIs(Set.of(ANIMAL_IRI, THING_IRI))
                .build();

        registry.register(animalMetadata);
        registry.register(dogMetadata);

        List<TypeMetadata<? extends Animal>> sorted = registry.getSortedMetadataForType(Animal.class);
        assertEquals(2, sorted.size());
        // Dog should come first (more specific, higher depth)
        assertEquals(Dog.class, sorted.get(0).getType());
        assertEquals(Animal.class, sorted.get(1).getType());
    }

    @Test
    public void testUnregister() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata);
        assertTrue(registry.isRegistered(Animal.class));
        assertTrue(registry.isRegistered(ANIMAL_IRI));

        boolean removed = registry.unregister(Animal.class);
        assertTrue(removed);
        assertFalse(registry.isRegistered(Animal.class));
        assertFalse(registry.isRegistered(ANIMAL_IRI));
    }

    @Test
    public void testUnregisterBySource() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        TypeMetadata<Dog> dogMetadata = SimpleTypeMetadata.<Dog>builder()
                .type(Dog.class)
                .typeIRI(DOG_IRI)
                .parentTypeIRIs(Set.of(ANIMAL_IRI, THING_IRI))
                .build();

        registry.register(animalMetadata, "bundle1");
        registry.register(dogMetadata, "bundle1");

        assertEquals(2, registry.size());

        registry.unregisterBySource("bundle1");
        assertEquals(0, registry.size());
    }

    @Test
    public void testGetRegisteredTypes() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata);

        Set<Class<? extends Thing>> types = registry.getRegisteredTypes();
        assertEquals(1, types.size());
        assertTrue(types.contains(Animal.class));
    }

    @Test
    public void testGetRegisteredTypeIRIs() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata);

        Set<IRI> iris = registry.getRegisteredTypeIRIs();
        assertEquals(1, iris.size());
        assertTrue(iris.contains(ANIMAL_IRI));
    }

    @Test
    public void testClear() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        registry.register(animalMetadata, "bundle1");
        assertEquals(1, registry.size());

        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    public void testIsOrExtendsType() {
        TypeMetadata<Dog> dogMetadata = SimpleTypeMetadata.<Dog>builder()
                .type(Dog.class)
                .typeIRI(DOG_IRI)
                .parentTypeIRIs(Set.of(ANIMAL_IRI, THING_IRI))
                .build();

        assertTrue(dogMetadata.isOrExtendsType(DOG_IRI));
        assertTrue(dogMetadata.isOrExtendsType(ANIMAL_IRI));
        assertTrue(dogMetadata.isOrExtendsType(THING_IRI));
        assertFalse(dogMetadata.isOrExtendsType(CAT_IRI));
    }

    @Test
    public void testHierarchyDepth() {
        TypeMetadata<Animal> animalMetadata = SimpleTypeMetadata.<Animal>builder()
                .type(Animal.class)
                .typeIRI(ANIMAL_IRI)
                .parentTypeIRIs(Set.of(THING_IRI))
                .build();

        TypeMetadata<Dog> dogMetadata = SimpleTypeMetadata.<Dog>builder()
                .type(Dog.class)
                .typeIRI(DOG_IRI)
                .parentTypeIRIs(Set.of(ANIMAL_IRI, THING_IRI))
                .build();

        assertEquals(1, animalMetadata.getHierarchyDepth());
        assertEquals(2, dogMetadata.getHierarchyDepth());
    }
}
