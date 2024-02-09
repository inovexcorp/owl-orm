package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SourceGenerator implements Runnable, ClosureIndex {

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    private final Model metamodel = MODEL_FACTORY.createEmptyModel();
    private final Set<ReferenceOntology> references;
    private final Set<GeneratingOntology> generateFor;
    private final String outputLocation;
    private final String prolog;
    private final FileSystemManager fileSystemManager;
    private final JCodeModel jCodeModel = new JCodeModel();


    @Builder
    protected SourceGenerator(@NonNull Set<OntologyMeta> generateForOntologies,
                              @NonNull Set<OntologyMeta> referenceOntologies,
                              @NonNull String outputLocation, Boolean enforceFullClosure) {
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
        this.references = new HashSet<>(referenceOntologies.size());
        referenceOntologies.forEach(wrapper -> {
            // Load our reference data into a model and create a ReferenceOntology instance.
            final Model model = loadOntologyModel(wrapper.getFile());
            this.references.add(ReferenceOntology.builder()
                    .codeModel(jCodeModel)
                    .ontologyModel(model)
                    .packageName(wrapper.getPackageName())
                    .ontologyName(wrapper.getOntologyName())
                    .build());
            // Dump all reference models into our meta model.
            this.metamodel.addAll(model);
        });
        // Initialize our target ontologies to generate.
        generateFor = new HashSet<>(generateForOntologies.size());
        generateForOntologies.forEach(wrapper -> generateFor.add(GeneratingOntology.builder()
                .useCodeModel(jCodeModel)
                .useOntologyModel(loadOntologyModel(wrapper.getFile()))
                .useReferenceModel(metamodel)
                .useClosureIndex(this)
                .useOntologyPackage(wrapper.getPackageName())
                .useOntologyName(wrapper.getOntologyName())
                .useEnforceFullClosure(enforceFullClosure)
                .build()));
    }

    @Override
    public void run() {
        // Generate the output sources
        writeSources();
    }

    @Override
    public Optional<JClass> findClassReference(GeneratingOntology generating, Resource resource) {
        JClass ref = generating.getClassIndex().get(resource);
        if (ref == null) {
            return references.stream().map(refOnt -> refOnt.getClassIndex().get(resource)).filter(Objects::nonNull)
                    .map(jCodeModel::ref)
                    .findFirst();
        } else {
            return Optional.of(ref);
        }
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
