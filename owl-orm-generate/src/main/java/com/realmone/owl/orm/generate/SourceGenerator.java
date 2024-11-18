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

import com.realmone.owl.orm.OrmException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SourceGenerator implements Runnable {

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    private final Model metamodel = MODEL_FACTORY.createEmptyModel();
    @Getter
    private final Set<ReferenceOntology> references;
    @Getter
    private final Set<GeneratingOntology> generateFor;
    @Getter
    private final JCodeModel jCodeModel = new JCodeModel();
    private final String outputLocation;
    private final String prolog;
    private final FileSystemManager fileSystemManager;


    @Builder
    protected SourceGenerator(@NonNull Set<OntologyMeta> generateForOntologies,
                              @NonNull Set<OntologyMeta> referenceOntologies,
                              @NonNull String outputLocation, Boolean enforceFullClosure,
                              Boolean isolateGenerationClosures) {
        final boolean includeGeneratedOntologiesInReferences = isolateGenerationClosures == null
                || !isolateGenerationClosures;
        this.outputLocation = outputLocation;
        try {
            fileSystemManager = VFS.getManager();
        } catch (FileSystemException e) {
            throw new OrmGenerationException("Issue initializing VFS system to fetch ontologies!", e);
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("prolog.txt")) {
            if (is != null) {
                this.prolog = IOUtils.toString(is, Charset.defaultCharset());
            } else {
                throw new OrmGenerationException("Prolog file could not be found");
            }
        } catch (IOException e) {
            throw new OrmGenerationException("Issue loading prolog data for file headers", e);
        }
        // Initialize our reference closure.
        this.references = new HashSet<>(includeGeneratedOntologiesInReferences ?
                referenceOntologies.size() + generateForOntologies.size() : referenceOntologies.size());
        referenceOntologies.forEach(this::loadReference);
        if (isolateGenerationClosures == null || !isolateGenerationClosures) {
            generateForOntologies.forEach(this::loadReference);
        }
        // Initialize our target ontologies to generate.
        generateFor = new HashSet<>(generateForOntologies.size());
        generateForOntologies.forEach(wrapper -> generateFor.add(GeneratingOntology.builder()
                .useCodeModel(jCodeModel)
                .useOntologyModel(loadOntologyModel(wrapper.getFile()))
                .useReferenceModel(metamodel)
                .useSourceGenerator(this)
                .useOntologyPackage(wrapper.getPackageName())
                .useOntologyName(wrapper.getOntologyName())
                .useEnforceFullClosure(enforceFullClosure == null || enforceFullClosure)
                .build()));
    }

    private void loadReference(OntologyMeta wrapper) {
        // Load our reference data into a model and create a ReferenceOntology instance.
        final Model model = loadOntologyModel(wrapper.getFile());
        this.references.add(ReferenceOntology.builder()
                .codeModel(jCodeModel)
                .ontologyModel(model)
                .packageName(wrapper.getPackageName())
                .ontologyName(wrapper.getOntologyName())
                .sourceGenerator(this)
                .build());
        // Dump all reference models into our meta model.
        this.metamodel.addAll(model);
    }

    @Override
    public void run() {

        // Generate the output sources
        writeSources();
    }

    private void writeSources() {
        // Generate code on disk with prolog.
        try {
            jCodeModel.build(new PrologCodeWriter(new FileCodeWriter(new File(outputLocation),
                    StandardCharsets.UTF_8.name()), prolog));
        } catch (IOException e) {
            throw new OrmException("Issue writing out generated OWL ORM code model", e);
        }
    }

    private Model loadOntologyModel(String ontologyFile) {
        try (FileObject fileObject = fileSystemManager.resolveFile(ontologyFile);
             final InputStream is = fileObject.getContent().getInputStream()) {
            final Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileObject.getName().getBaseName());
            if (format.isPresent()) {
                return Rio.parse(is, format.get());
            } else {
                throw new OrmGenerationException("Could not identify format of file containing ontology: " + fileObject.getName());
            }
        } catch (IOException e) {
            throw new OrmGenerationException("Issue loading ontology model data", e);
        }
    }
}
