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
package com.realmone.maven;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.generate.OntologyMeta;
import com.realmone.owl.orm.generate.SourceGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "generate-orm", threadSafe = true)
public class GeneratorMojo extends AbstractMojo {

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    @Parameter(alias = "generates", required = true)
    private List<Ontology> generates;

    /**
     * List of reference ontologies.
     */
    @Parameter(alias = "references")
    private List<Ontology> references;

    /**
     * The location where the generated source will be stored.
     */
    @Parameter(property = "outputLocation", required = true,
            defaultValue = "${project.basedir}/target/generated-sources")
    private String outputLocation;

    @Parameter(property = "enforceFullClosure", required = true, defaultValue = "true")
    private boolean enforceFullClosure;

    @Parameter(property = "isolateGenerationClosures", required = true, defaultValue = "false")
    private boolean isolateGenerationClosures;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ensureDirectoryExists(outputLocation);
            SourceGenerator generator = SourceGenerator.builder()
                    .enforceFullClosure(enforceFullClosure)
                    .isolateGenerationClosures(isolateGenerationClosures)
                    .generateForOntologies(convertOnts(generates))
                    .referenceOntologies(convertOnts(references))
                    .outputLocation(outputLocation)
                    .build();
            generator.run();
        } catch (OrmException e) {
            throw new MojoFailureException("Issue occurred generating source for OWL ORM API", e);
        }
    }

    private static Set<OntologyMeta> convertOnts(List<Ontology> ontologies) {
        return ontologies != null ? ontologies.stream().map(GeneratorMojo::fromOnt).collect(Collectors.toSet())
                : Collections.emptySet();
    }

    private static OntologyMeta fromOnt(Ontology ont) {
        return OntologyMeta.builder()
                .file(ont.getOntologyFile())
                .ontologyName(ont.getOntologyName())
                .packageName(ont.getOutputPackage())
                .build();
    }

    private static void ensureDirectoryExists(String outputLocation) {
        File f = new File(outputLocation);
        if (!f.isDirectory() && (!f.mkdirs())) {
            throw new OrmException("Couldn't generate output directory: " + outputLocation);
        }
    }
}
