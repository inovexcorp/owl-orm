package com.realmone.owl.orm.generate;

import com.sun.codemodel.JCodeModel;
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
import java.io.IOException;
import java.io.Reader;

public class TestOntology {

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();
    private static final ValueFactory VALUE_FACTORY = new ValidatingValueFactory();

    private final File output = new File("target/test-generated-classes");


    private JCodeModel codeModel;
    private Model model;

    @Before
    public void clearOutput() throws IOException {
        FileUtils.deleteDirectory(output);
        if (!output.isDirectory()) {
            Assert.assertTrue(output.mkdirs());
        }
    }

    @Before
    public void initializeCodeModel() {
        codeModel = new JCodeModel();
    }

    @Before
    public void initModel() throws Exception {
        try (Reader reader = new FileReader("src/test/resources/BierOnto.ttl")) {
            this.model = Rio.parse(reader, RDFFormat.TURTLE);
        }
    }

    @Test
    public void basicTest() throws Exception {
        Ontology ontology = Ontology.builder()
                .useCodeModel(codeModel)
                .useOntologyPackage("com.realmone.bieronto")
                .useOntologyModel(model)
                .build();
        int classCount = model.filter(null, RDF.TYPE, OWL.CLASS).size();
        Assert.assertEquals("Expected one generated class per ontology",
                classCount, ontology.getClasses().size());
        Assert.assertEquals("Expected bieronto ontology IRI did not match",
                "https://mobi.com/ontologies/4/2018/BierOnto", ontology.getResource().stringValue());
        codeModel.build(output);
    }
}
