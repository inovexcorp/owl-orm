package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.generate.properties.DatatypeProperty;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.realmone.owl.orm.generate.support.NamingUtilities;
import com.realmone.owl.orm.generate.properties.ObjectProperty;
import com.sun.codemodel.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Generated;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.*;

public class GeneratingOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratingOntology.class);
    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    @Getter
    private final JPackage jPackage;
    @Getter
    private final Map<Resource, JDefinedClass> classIndex = new HashMap<>();
    @Getter
    private final Resource ontologyResource;
    @Getter
    private final Set<Resource> imports = new HashSet<>();
    @Getter
    private final Set<Resource> classIris = new HashSet<>();
    @Getter
    private final Map<Resource, Set<Resource>> classHierarchy = new HashMap<>();
    @Getter
    private final Model closureModel;
    @Getter
    private final Model model;
    @Getter
    private final Map<Resource, DatatypeProperty> datatypeProperties = new HashMap<>();
    @Getter
    private final Set<ObjectProperty> objectProperties = new HashSet<>();

    private final ClosureIndex closureIndex;

    @Builder(setterPrefix = "use")
    public GeneratingOntology(@NonNull JCodeModel codeModel, @NonNull Model ontologyModel,
                              @NonNull Model referenceModel, @NonNull String ontologyName,
                              @NonNull String ontologyPackage, @NonNull ClosureIndex closureIndex,
                              Boolean enforceFullClosure) throws OrmException {
        this.jPackage = codeModel._package(ontologyPackage);
        this.closureIndex = closureIndex;
        this.model = ontologyModel;
        this.ontologyResource = getOntologyResource(model, ontologyName);
        this.closureModel = MODEL_FACTORY.createEmptyModel();
        closureModel.addAll(ontologyModel);
        closureModel.addAll(referenceModel);
        Set<Resource> missingOntologies = GraphUtils.missingOntologies(closureModel, ontologyResource);
        if (!missingOntologies.isEmpty()) {
            if (enforceFullClosure == null || enforceFullClosure) {
                throw new OrmGenerationException(String.format("Ontology %s is missing import(s): %s",
                        ontologyResource.stringValue(), StringUtils.join(missingOntologies, ", ")));
            } else if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(String.format("Ontology %s is missing import(s): %s",
                        ontologyResource.stringValue(), StringUtils.join(missingOntologies, ", ")));
            }
        }
        // Warn about missing ontologies.
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
        model.filter(ontologyResource, OWL.IMPORTS, null).objects().stream()
                // Convert from Value to Resource and add to our set.
                .map(this::toResource).forEach(imports::add);
        // Add all class resources to our index.
        // TODO - RDFS.CLASS support?
        classIris.addAll(model.filter(null, RDF.TYPE, OWL.CLASS).subjects());
        // Build our hierarchy index map.
        classIris.forEach(classResource -> {
            classIndex.put(classResource, generateInterface(classResource));
            // Add our class to the hierarchy
            classHierarchy.put(classResource, GraphUtils.lookupParentClasses(closureModel, classResource));
        });
        classHierarchy.forEach((classResource, parents) -> {
            JDefinedClass clazz = classIndex.get(classResource);
            parents.forEach(parentResource -> {
                JClass ref = closureIndex.findClassReference(this, parentResource)
                        //TODO - better error message
                        .orElseThrow(() -> new OrmGenerationException("Couldn't find parent class in index: " +
                                parentResource));
                clazz._implements(ref);
            });
        });
        // Start our index of Datatype Properties
        model.filter(null, RDF.TYPE, OWL.DATATYPEPROPERTY).subjects()
                .forEach(propResource -> datatypeProperties.put(propResource,
                        DatatypeProperty.builder()
                                .useResource(propResource)
                                .useFunctional(GraphUtils.lookupFunctional(model, propResource))
                                .useCodeModel(jPackage.owner())
                                .useJavaName(NamingUtilities.getPropertyName(model, ontologyResource))
                                .useRangeIri(GraphUtils.lookupRange(model, propResource))
                                .build()));
//        model.filter(null, RDF.TYPE, OWL.OBJECTPROPERTY).subjects();
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
                    .param("value", SourceGenerator.class.getName())
                    .param("date", ZonedDateTime.now().toString())
                    .param("comments", String.format("Generated by OWL ORM Maven Plugin for ontology: %s",
                            resource.stringValue()));
        }
    }

    private Resource toResource(Value value) {
        return (Resource) value;
    }
}
