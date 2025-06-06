/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.VocabularyIRIs;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.generate.properties.DatatypeProperty;
import com.realmone.owl.orm.generate.properties.ObjectProperty;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.realmone.owl.orm.generate.support.NamingUtilities;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Generated;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class GeneratingOntology extends AbstractOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratingOntology.class);

    private final JPackage jPackage;
    private final String ontologyName;
    private final Resource ontologyResource;
    private final JDefinedClass ontologyThing;
    private final Set<Resource> imports = new HashSet<>();
    private final Set<Resource> classIris = new HashSet<>();
    private final Map<Resource, Set<Resource>> classHierarchy = new HashMap<>();
    private final Model model;
    private final Map<Resource, DatatypeProperty> datatypeProperties = new HashMap<>();
    private final Map<Resource, ObjectProperty> objectProperties = new HashMap<>();

    @Builder(setterPrefix = "use")
    public GeneratingOntology(@NonNull JCodeModel codeModel, @NonNull Model ontologyModel,
                              @NonNull Model referenceModel, @NonNull String ontologyName,
                              @NonNull String ontologyPackage, @NonNull SourceGenerator sourceGenerator,
                              boolean enforceFullClosure) throws OrmException {
        super(sourceGenerator, codeModel, enforceFullClosure);

        this.jPackage = codeModel._package(ontologyPackage);
        this.model = ontologyModel;
        this.ontologyName = ontologyName;
        this.ontologyResource = getOntologyResource(model);
        closureModel.addAll(ontologyModel);
        closureModel.addAll(referenceModel);
        this.ontologyThing = generateOntologyThing();
        Set<Resource> missingOntologies = GraphUtils.missingOntologies(closureModel, ontologyResource);
        if (!missingOntologies.isEmpty()) {
            if (enforceFullClosure) {
                throw new OrmGenerationException(String.format("Ontology %s is missing import(s): %s",
                        ontologyResource.stringValue(), StringUtils.join(missingOntologies, ", ")));
            } else if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Ontology {} is missing import(s): {}", ontologyResource.stringValue(),
                        StringUtils.join(missingOntologies, ", "));
            }
        }
        // Warn about missing ontologies.
        analyzeAndGenerate();
    }

    private Resource getOntologyResource(Model model) {
        final Set<Resource> ontologiesInModel = model.filter(null, RDF.TYPE, OWL.ONTOLOGY).subjects();
        if (ontologiesInModel.size() > 1) {
            throw new OrmException(String.format("More than one ontology in file '%s': %s",
                    ontologyName, ontologiesInModel));
        } else if (ontologiesInModel.isEmpty()) {
            throw new OrmException(String.format("No ontology defined in file '%s'", ontologyName));
        } else {
            // For each ontology in the data -- should be exactly one.
            ontologiesInModel.forEach(resource ->
                    // Put in our index the resource of that ontology
                    model.filter(resource, OWL.IMPORTS, null).objects().stream()
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
        classIris.addAll(model.filter(null, RDF.TYPE, OWL.CLASS).subjects()
                .stream()
                .filter(Resource::isIRI) // Don't create interfaces for blank node classes (usually Restrictions)
                .collect(Collectors.toSet())
        );
        // Build our hierarchy index map.
        classIris.forEach(classResource -> {
            classIndex.put(classResource, generateInterface(classResource));
            // Add our class to the hierarchy
            classHierarchy.put(classResource, GraphUtils.lookupParentClasses(closureModel, classResource,
                    enforceFullClosure));
        });
        // Define the class hierarchy
        classHierarchy.forEach((classResource, parents) -> {
            try {
                JDefinedClass clazz = (JDefinedClass) classIndex.get(classResource);
                // Add the type IRI/resource to the class as a static field
                clazz.field(JMod.STATIC, String.class, "TYPE_STR", JExpr.lit(classResource.stringValue()))
                        .javadoc().add("The String value of the rdf:type that identifies this class.");
                if (classResource.isIRI()) {
                    IRI classIRI = (IRI) classResource;
                    clazz.field(JMod.STATIC, IRI.class, "TYPE", jPackage.owner().ref(VocabularyIRIs.class)
                                    .staticInvoke("createIRI")
                                    .arg(classIRI.getNamespace())
                                    .arg(classIRI.getLocalName()))
                            .javadoc().add("The IRI value of the rdf:type that identifies this class.");
                }
                parents.forEach(parentResource -> {
                    Optional<JClass> optionalParent = findClassReference(parentResource);
                    // If the parent class is present in the closure
                    if (optionalParent.isPresent()) {
                        clazz._implements(optionalParent.get());
                    }
                    // Else if we're configured to enforce the full closure we should throw an exception
                    else if (enforceFullClosure) {
                        throw new OrmGenerationException("Couldn't find parent class in index: " + parentResource);
                    }
                    // Else we should log a warning to the user, and trust they know what they're doing :)
                    else {
                        log.warn("Couldn't find parent class of '{}' in closure: {}", classResource.stringValue(),
                                parentResource.stringValue());
                    }
                });
            } catch (ClassCastException e) {
                //TODO - better error handling...
                throw new OrmGenerationException("", e);
            }
        });
        // Build out datatype properties.
        model.filter(null, RDF.TYPE, OWL.DATATYPEPROPERTY).subjects()
                .forEach(propResource -> datatypeProperties.put(propResource,
                        DatatypeProperty.builder()
                                .useClosureIndex(this)
                                .useResource(propResource)
                                .useFunctional(GraphUtils.lookupFunctional(model, propResource))
                                .useCodeModel(jPackage.owner())
                                .useJavaName(NamingUtilities.getPropertyName(model, propResource))
                                .useRangeIri(GraphUtils.lookupRange(model, propResource))
                                .useDomains(GraphUtils.lookupDomain(model, propResource))
                                .build()));
        // Build out object properties.
        model.filter(null, RDF.TYPE, OWL.OBJECTPROPERTY).subjects()
                .forEach(propResource -> objectProperties.put(propResource,
                        ObjectProperty.builder()
                                .useClosureIndex(this)
                                .useResource(propResource)
                                .useCodeModel(jPackage.owner())
                                .useRangeResource(GraphUtils.lookupRange(closureModel, propResource))
                                .useDomains(GraphUtils.lookupDomain(model, propResource))
                                .useFunctional(GraphUtils.lookupFunctional(closureModel, propResource))
                                .useJavaName(NamingUtilities.getPropertyName(model, propResource))
                                .build()));
        // Attach properties to interfaces...
        datatypeProperties.forEach((propResource, property) ->
        {
            property.getDomain().stream().map(classIndex::get)
                    .filter(JDefinedClass.class::isInstance)
                    .map(JDefinedClass.class::cast)
                    .forEach(property::attach);
            if (property.getDomain().isEmpty()) {
                property.attach(ontologyThing);
            }
        });
        objectProperties.forEach((propResource, property) -> {
            property.getDomain().stream().map(classIndex::get)
                    .filter(JDefinedClass.class::isInstance)
                    .map(JDefinedClass.class::cast)
                    .forEach(property::attach);
            if (property.getDomain().isEmpty()) {
                property.attach(ontologyThing);
            }
        });
    }

    private JDefinedClass generateOntologyThing() throws OrmException {
        try {
            JDefinedClass ontThing = jPackage._interface(JMod.PUBLIC,
                    NamingUtilities.safeName(ontologyName + " Thing", true));
            ontThing._extends(Thing.class);
            // Annotate?
            // Javadoc?
            return ontThing;
        } catch (JClassAlreadyExistsException e) {
            throw new OrmException("Issue generating ontology thing; already exists!", e);
        }
    }

    private JDefinedClass generateInterface(Resource resource)
            throws OrmException {
        try {
            // Create the interface in our package.
            JDefinedClass interfaze = jPackage._interface(JMod.PUBLIC,
                    NamingUtilities.getClassName(model, resource));
            interfaze._extends(ontologyThing);
            // Annotate the interface with our @Type interface to help wire proxy objects later.
            interfaze.annotate(Type.class).param("value", resource.stringValue());
            // Mark up the interface with appropriate comments and standard annotations.
            markupInterface(interfaze, resource);
            return interfaze;
        } catch (JClassAlreadyExistsException e) {
            throw new OrmException(String.format("Cannot generate class as it already exists in package: %s",
                    resource.stringValue()));
        }
    }

    private void markupInterface(JDefinedClass interfaze, Resource resource) {
        JDocComment comment = interfaze.javadoc();
        comment.add(String.format("<p>Interface generated for class '%s'</p>%n", resource.stringValue()));
        comment.add(GraphUtils.printModelForJavadoc(findContext(resource)));
        // Annotate the class with the @Generated annotation and relevant metadata.
        interfaze.annotate(Generated.class)
                .param("value", SourceGenerator.class.getName())
                .param("date", ZonedDateTime.now().toString())
                .param("comments", String.format("Generated by OWL ORM Maven Plugin for ontology: %s",
                        resource.stringValue()));

    }

    private Resource toResource(Value value) {
        return (Resource) value;
    }
}
