/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
    public void testGenerate() {
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
                .enforceFullClosure(true)
                .build();
        gen.run();
    }

    @Test(expected = OrmGenerationException.class)
    public void testMissingClosure() {
        /*
        BierOnto imports foaf, but we're not passing it in.  Generation should fail fast.
         */
        Set<OntologyMeta> generate = singletonSet(OntologyMeta.builder()
                .file(new File("src/test/resources/BierOnto.ttl").getAbsolutePath())
                .ontologyName("BierOnto")
                .packageName("com.realmone.bieronto")
                .build());
        SourceGenerator.builder()
                .enforceFullClosure(true)
                .outputLocation("target/")
                .generateForOntologies(generate)
                .referenceOntologies(Collections.emptySet())
                .build();
    }

    private Set<OntologyMeta> singletonSet(OntologyMeta wrapper) {
        return Collections.singleton(wrapper);
    }
}
