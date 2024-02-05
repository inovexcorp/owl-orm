package com.realmone.maven;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.annotations.Type;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.PrologCodeWriter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import javax.annotation.Generated;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OwlOrmGenerator implements Runnable {

    @Getter
    private final Model referenceClosure;
    @Getter
    private final List<Ontology> generate;
    @Getter
    private final List<Ontology> references;
    @Getter
    private final JCodeModel jCodeModel = new JCodeModel();
    @Getter
    private final long size;
    private final Log log;
    private final String outputLocation;
    private final String prolog;

    @Builder(setterPrefix = "use")
    protected OwlOrmGenerator(@NonNull Model model, @NonNull List<Ontology> generate,
                              @NonNull List<Ontology> references, @NonNull FileSystemManager fileSystemManager,
                              @NonNull String outputLocation, @NonNull String prolog, @NonNull Log log) {
        this.referenceClosure = model;
        this.generate = generate;
        this.references = references;
        // Combine all the reference model closure
        references.forEach(ontology -> referenceClosure.addAll(ontology.loadModel(fileSystemManager)));
        // Isolate each generation model.
        generate.forEach(ontology -> ontology.loadModel(fileSystemManager));
        this.size = this.getGenerate().stream()
                .map(Ontology::getModel).map(Model::size).mapToLong(Integer::longValue).sum()
                + this.getReferences().stream()
                .map(Ontology::getModel).map(Model::size).mapToLong(Integer::longValue).sum();
        this.outputLocation = outputLocation;
        this.prolog = prolog;
        this.log = log;
    }

    @Override
    public void run() {
        for (Ontology ontology : generate) {
            // Threading?
            generateCodeForOntology(ontology, outputLocation);
        }
        // Generate code on disk with prolog.
        try {
            jCodeModel.build(new PrologCodeWriter(new FileCodeWriter(new File(outputLocation),
                    StandardCharsets.UTF_8.name()), prolog));
        } catch (IOException e) {
            throw new OrmException("Issue writing out generated OWL ORM code model", e);
        }
    }

    private void generateCodeForOntology(Ontology ontology, String outputLocation) throws OrmException {
        log.debug(String.format("Generating source code for ontology '%s' to: %s",
                ontology.getResource(), outputLocation));
        final JPackage jPackage = jCodeModel._package(ontology.getOutputPackage());
        ontology.getClasses().forEach(resource -> {
            JDefinedClass interfaze = generateInterface(ontology, jPackage, resource);
            ontology.getResourceClassMap().put(resource, interfaze);
            log.debug(String.format("Generated class '%s' for resource: %s",
                    interfaze.fullName(), resource.stringValue()));
            Set<Resource> parents = ontology.getClassHierarchy().get(resource);
            if (parents == null || parents.isEmpty()) {
                log.debug(String.format("Class '%s' has no hierarchy annotations (rdfs:subClassOf)",
                        resource.stringValue()));
            } else {
                parents.forEach(parent ->{
                    if(ontology.getModel().filter(parent, RDF.TYPE, OWL.CLASS).isEmpty()){

                    }else{
                        Ontology parentLivesIn = whereIsReferenceEntityDefined(parent).orElse(null);

                    }
                });
            }
        });
    }

    private JDefinedClass generateInterface(Ontology ontology, JPackage jPackage, Resource resource) throws OrmException {
        try {
            // Create the interface in our package.
            JDefinedClass interfaze = jPackage._interface(JMod.PUBLIC, getClassName(ontology.getModel(), resource));
            // Annotate the interface with our @Type interface to help wire proxy objects later.
            interfaze.annotate(Type.class).param("value", resource.stringValue());
            // Mark up the interface with appropriate comments and standard annotations.
            markupInterface(ontology, interfaze, resource);
            return interfaze;
        } catch (JClassAlreadyExistsException e) {
            throw new OrmException(String.format("Cannot generate class as it already exists in package: %s",
                    resource.stringValue()));
        } catch (IOException e) {
            throw new OrmException("Issue writing RDF for comments", e);
        }
    }

    private void markupInterface(Ontology ontology, JDefinedClass interfaze, Resource resource) throws IOException {
        try (Writer writer = new StringWriter()) {
            Rio.write(ontology.getModel().filter(resource, null, null), writer, RDFFormat.TURTLE);
            JDocComment comment = interfaze.javadoc();
            comment.add(String.format("Interface generated for class '%s'%n*************************",
                    resource.stringValue()));
            comment.add(writer.toString());
            comment.add("*************************");
            // Annotate the class with the @Generated annotation and relevant metadata.
            interfaze.annotate(Generated.class)
                    .param("value", getClass().getName())
                    .param("date", ZonedDateTime.now().toString())
                    .param("comments", String.format("Generated by OWL ORM Maven Plugin for ontology: %s",
                            ontology.getResource().stringValue()));
        }
    }

    private Optional<Ontology> whereIsReferenceEntityDefined(Resource resource) {
        return references.stream()
                // Find a reference ontology that defines.
                .filter(ontology -> ontology.getModel().subjects().contains(resource))
                // Find the first match.
                .findFirst();
    }

    private static String getClassName(Model closure, Resource resource) {
        return safeName(getNameTemplate(closure, resource), true);
    }

    private static String getNameTemplate(Model closure, Resource resource) {
        // DCTERMS:title, RDFS:label, then IRI localname
        return closure.filter(resource, DCTERMS.TITLE, null).objects().stream().findFirst().map(Value::stringValue)
                .orElseGet(() ->
                        closure.filter(resource, RDFS.LABEL, null).objects().stream().findFirst()
                                .map(Value::stringValue)
                                .orElseGet(() ->
                                        (resource.isIRI()) ? ((IRI) resource).getLocalName() : resource.stringValue()));
    }

    /**
     * Simple method to strip whitespaces from the name. It will also ensure it
     * is a valid class or field name.
     *
     * @param input The input string
     * @return The stripped and cleaned output name
     */
    protected static String safeName(final String input, boolean capitalizeFirst) {
        StringBuilder builder = new StringBuilder();
        boolean lastIsWhiteSpace = false;
        boolean first = true;
        for (char c : input.toCharArray()) {
            if (first && !Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(c)) {
                builder.append("_");
                builder.append(capitalizeFirst ? Character.toUpperCase(c) : c);
                first = false;
            } else if (Character.isWhitespace(c)) {
                lastIsWhiteSpace = true;
            } else if (Character.isJavaIdentifierPart(c)) {
                builder.append(lastIsWhiteSpace || (first && capitalizeFirst) ? StringUtils.capitalize(c + "") : c);
                lastIsWhiteSpace = false;
                first = false;
            }
        }
        return builder.toString();
    }
}
