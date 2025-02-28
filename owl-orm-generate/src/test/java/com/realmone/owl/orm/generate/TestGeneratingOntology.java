/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate;

import com.sun.codemodel.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Slf4j
public class TestGeneratingOntology {

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();
    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();
    private static final File OUTPUT_DIR = new File("target/test-generated-classes");
    private static final File BIERONTO = new File("src/test/resources/BierOnto.ttl");
    private static final File FOAF = new File("src/test/resources/foaf.owl");

    private JCodeModel codeModel;
    private Model model;
    private Model referenceModel;

    @Before
    public void setupTests() throws Exception {
        FileUtils.deleteDirectory(OUTPUT_DIR);
        if (!OUTPUT_DIR.isDirectory()) {
            Assert.assertTrue(OUTPUT_DIR.mkdirs());
        }
        codeModel = new JCodeModel();
        try (Reader reader = new FileReader(BIERONTO)) {
            this.model = Rio.parse(reader, RDFFormat.TURTLE);
        }
        try (Reader reader = new FileReader(FOAF)) {
            this.referenceModel = Rio.parse(reader, "http://xmlns.com/foaf/0.1/", RDFFormat.RDFXML);
        }
    }

    //TODO write tests!

    @Test
    public void basicTest() throws Exception {
        SourceGenerator gen = SourceGenerator.builder()
                .enforceFullClosure(true)
                .isolateGenerationClosures(false)
                .generateForOntologies(Set.of())
                .referenceOntologies(Set.of(
                        OntologyMeta.builder()
                                .file(FOAF.getAbsolutePath())
                                .packageName("com.realmone.owl.orm.generate")
                                .ontologyName("FOAF")
                                .build()
                ))
                .outputLocation(OUTPUT_DIR.getAbsolutePath())
                .build();
        GeneratingOntology ontology = GeneratingOntology.builder()
                .useSourceGenerator(gen)
                .useCodeModel(codeModel)
                .useOntologyPackage("com.realmone.bieronto")
                .useOntologyModel(model)
                .useOntologyName("BierOnto")
                .useReferenceModel(referenceModel)
                .build();
        int classCount = model.filter(null, RDF.TYPE, OWL.CLASS).size();
        Assert.assertEquals("Expected one generated class per ontology",
                classCount, ontology.getClassIris().size());
        Assert.assertEquals("Expected bieronto ontology IRI did not match",
                "https://mobi.com/ontologies/4/2018/BierOnto", ontology.getOntologyResource().stringValue());
        codeModel.build(OUTPUT_DIR);
        validateGeneratedClasses(codeModel);
    }

    public static void validateGeneratedClasses(JCodeModel codeModel) {
        Iterator<JPackage> packageIterator = codeModel.packages();
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(packageIterator, 0), false)
                .flatMap(pkg -> {
                    Iterator<JDefinedClass> classIterator = pkg.classes();
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(classIterator, 0), false);
                })
                .forEach(jClass -> {
                    if (jClass.annotations().stream().anyMatch(ann -> ann.getAnnotationClass()
                            .fullName().equals("com.realmone.owl.orm.annotations.Type"))) {
                        log.debug("Found generated class for annotation: {}", jClass.fullName());
                        JFieldVar typeField = jClass.fields().get("TYPE");
                        Assert.assertNotNull("Class " + jClass.name() + " is missing type field", typeField);
                        Assert.assertEquals("Class " + jClass.name() + " has type field that is not static",
                                JMod.STATIC, typeField.mods().getValue());
                    } else {
                        log.debug("Skipping generated class for annotation: {}", jClass.name());
                    }
                });
    }
}
