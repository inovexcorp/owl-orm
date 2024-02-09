package com.realmone.owl.orm.generate;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class TestSourceGenerator {

    private final File output = new File("target/source-gen-test");

    @Before
    public void clearOutput() throws Exception {
        FileUtils.deleteDirectory(output);
        Assert.assertTrue("Couldn't clear out and remake output location", output.mkdirs());
    }


    @Test
    public void test() {
        // Create our request objects.

        Set<OntologyMeta> generate = singletonSet(OntologyMeta.builder()
                .file(new File("src/test/resources/BierOnto.ttl").getAbsolutePath())
                .ontologyName("BierOnto")
                .packageName("com.realmone.bieronto")
                .build());
        Set<OntologyMeta> reference = singletonSet(OntologyMeta.builder()
                .file(new File("src/test/resources/foaf.owl").getAbsolutePath())
                .packageName("org.foaf")
                .ontologyName("Friend of a Friend")
                .build());
        // Initialize our generator object.
        SourceGenerator gen = SourceGenerator.builder()
                .generateForOntologies(generate)
                .referenceOntologies(reference)
                .outputLocation("target/source-gen-test")
                .build();
        gen.run();
    }

    private Set<OntologyMeta> singletonSet(OntologyMeta wrapper) {
        return Collections.singleton(wrapper);
    }
}
