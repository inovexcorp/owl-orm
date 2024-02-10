package com.realmone.owl.orm.generate;

import com.sun.codemodel.JCodeModel;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;

public class TestReferenceOntology {

    private Model model;

    private ValueFactory VF = new ValidatingValueFactory();

    @Before
    public void initModel() throws Exception {
        try (Reader reader = new FileReader("src/test/resources/BierOnto.ttl")) {
            this.model = Rio.parse(reader, RDFFormat.TURTLE);
        }
    }

    @Test
    @Ignore
    public void testReferenceOnt() {
        ReferenceOntology ont = ReferenceOntology.builder()
                .ontologyName("BierOnto")
                .codeModel(new JCodeModel())
                .ontologyModel(model)
                .packageName("com.realmone.bieronto")
                .build();
        Assert.assertEquals(19, ont.getClassIndex().size());
    }
}
