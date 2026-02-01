/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.TypeMetadata;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
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
        // Load prolog header.
        this.prolog = loadProlog();
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

    private String loadProlog(){
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("prolog.txt")) {
            if (is != null) {
                return IOUtils.toString(is, Charset.defaultCharset());
            } else {
                throw new OrmGenerationException("Prolog file could not be found");
            }
        } catch (IOException e) {
            throw new OrmGenerationException("Issue loading prolog data for file headers", e);
        }
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
        // Generate TypeMetadata classes for all ontologies
        generateFor.forEach(GeneratingOntology::generateTypeMetadataClasses);

        // Generate the output sources
        writeSources();

        // Generate ServiceLoader configuration file
        writeServiceLoaderConfig();
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

    /**
     * Writes the ServiceLoader configuration file (META-INF/services/com.realmone.owl.orm.TypeMetadata)
     * containing all generated TypeMetadata class names. This enables automatic discovery of type
     * metadata using the Java ServiceLoader mechanism.
     */
    private void writeServiceLoaderConfig() {
        // Collect all TypeMetadata class names
        List<String> metadataClassNames = new ArrayList<>();
        for (GeneratingOntology ontology : generateFor) {
            for (JDefinedClass metadataClass : ontology.getTypeMetadataClasses().values()) {
                metadataClassNames.add(metadataClass.fullName());
            }
        }

        if (metadataClassNames.isEmpty()) {
            log.debug("No TypeMetadata classes generated, skipping ServiceLoader config");
            return;
        }

        // Create META-INF/services directory
        Path servicesDir = Path.of(outputLocation, "META-INF", "services");
        try {
            Files.createDirectories(servicesDir);
        } catch (IOException e) {
            throw new OrmException("Failed to create META-INF/services directory", e);
        }

        // Write the service configuration file
        Path serviceFile = servicesDir.resolve(TypeMetadata.class.getName());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(serviceFile.toFile(), StandardCharsets.UTF_8))) {
            writer.write("# Generated by OWL ORM Code Generator");
            writer.newLine();
            writer.write("# TypeMetadata implementations for ServiceLoader discovery");
            writer.newLine();
            for (String className : metadataClassNames) {
                writer.write(className);
                writer.newLine();
            }
            log.info("Generated ServiceLoader config with {} TypeMetadata classes at {}",
                    metadataClassNames.size(), serviceFile);
        } catch (IOException e) {
            throw new OrmException("Failed to write ServiceLoader configuration file", e);
        }
    }
}
