package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.annotations.Type;
import com.sun.codemodel.*;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import javax.annotation.processing.Generated;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Ontology {

    @Getter
    private final JPackage jPackage;
    private final Map<Resource, JDefinedClass> classIndex = new HashMap<>();
    @Getter
    private final Resource resource;
    @Getter
    private final Set<Resource> imports = new HashSet<>();
    @Getter
    private final Set<Resource> classes = new HashSet<>();
    @Getter
    private final Map<Resource, Set<Resource>> classHierarchy = new HashMap<>();
    @Getter
    private final Set<Resource> datatypeProperties = new HashSet<>();
    @Getter
    private final Set<Resource> objectProperties = new HashSet<>();
    @Getter
    private final Model model;

    @Builder(setterPrefix = "use")
    public Ontology(JCodeModel codeModel, Model ontologyModel, String ontologyName,
                    String ontologyPackage) throws OrmException {
        this.jPackage = codeModel._package(ontologyPackage);
        this.model = ontologyModel;
        this.resource = getOntologyResource(model, ontologyName);
        analyzeAndGenerate();
    }

    private Resource getOntologyResource(Model model, String ontologyName) {
        final Set<Resource> ontologiesInModel = model.filter(null, RDF.TYPE, OWL.ONTOLOGY).subjects();
        if (ontologiesInModel.size() > 1) {
            throw new OrmException(String.format("More than one ontology in file '%s': %s",
                    ontologyName, ontologiesInModel));
        } else if (ontologiesInModel.isEmpty()) {
            throw new OrmException(String.format("No ontology defined in file '%s'", ontologyName));
        } else {
            // For each ontology in the data -- should be exactly one.
            ontologiesInModel.forEach(ontologyResource ->
                    // Put in our index the resource of that ontology
                    model.filter(ontologyResource, OWL.IMPORTS, null).objects().stream()
                            // Convert from Value to Resource and add to our set.
                            .map(this::toResource).forEach(imports::add)
            );
            return ontologiesInModel.stream().findFirst().orElseThrow();
        }
    }

    private void analyzeAndGenerate() throws OrmException {
        // Find all the imports of this particular model.
        model.filter(resource, OWL.IMPORTS, null).objects().stream()
                // Convert from Value to Resource and add to our set.
                .map(this::toResource).forEach(imports::add);
        // Add all class resources to our index.
        // TODO - RDFS.CLASS support?
        classes.addAll(model.filter(null, RDF.TYPE, OWL.CLASS).subjects());
        // Build our hierarchy index map.
        classes.forEach(clazz -> {
            classIndex.put(clazz, generateInterface(clazz));
            // Add our class to the hierarchy
            classHierarchy.put(clazz, model.filter(clazz, RDFS.SUBCLASSOF, null)
                    // Find the subClassOf properties, and collect into a set of resources.
                    .objects().stream().map(Resource.class::cast).collect(Collectors.toSet()));
        });
        datatypeProperties.addAll(model.filter(null, RDF.TYPE, OWL.DATATYPEPROPERTY).subjects());
        objectProperties.addAll(model.filter(null, RDF.TYPE, OWL.OBJECTPROPERTY).subjects());
    }

    private JDefinedClass generateInterface(Resource resource)
            throws OrmException {
        try {
            // Create the interface in our package.
            JDefinedClass interfaze = jPackage._interface(JMod.PUBLIC,
                    NamingUtilities.getClassName(model, resource));
            // Annotate the interface with our @Type interface to help wire proxy objects later.
            interfaze.annotate(Type.class).param("value", resource.stringValue());
            // Mark up the interface with appropriate comments and standard annotations.
            markupInterface(interfaze, resource);
            return interfaze;
        } catch (JClassAlreadyExistsException e) {
            throw new OrmException(String.format("Cannot generate class as it already exists in package: %s",
                    resource.stringValue()));
        } catch (IOException e) {
            throw new OrmException("Issue writing RDF for comments", e);
        }
    }

    private void markupInterface(JDefinedClass interfaze, Resource resource) throws IOException {
        try (Writer writer = new StringWriter()) {
            Rio.write(model.filter(resource, null, null), writer, RDFFormat.TURTLE);
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
                            resource.stringValue()));
        }
    }

    private Resource toResource(Value value) {
        return (Resource) value;
    }
}
