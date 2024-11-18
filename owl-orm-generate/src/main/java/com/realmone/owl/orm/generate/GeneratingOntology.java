package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.annotations.Type;
import com.realmone.owl.orm.generate.properties.DatatypeProperty;
import com.realmone.owl.orm.generate.properties.ObjectProperty;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.realmone.owl.orm.generate.support.NamingUtilities;
import com.sun.codemodel.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Set;

@Getter
public class GeneratingOntology extends AbstractOntology implements ClosureIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratingOntology.class);

    private final JPackage jPackage;
    private final Resource ontologyResource;
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
        super(sourceGenerator, codeModel);
        this.jPackage = codeModel._package(ontologyPackage);
        this.model = ontologyModel;
        this.ontologyResource = getOntologyResource(model, ontologyName);
        closureModel.addAll(ontologyModel);
        closureModel.addAll(referenceModel);
        Set<Resource> missingOntologies = GraphUtils.missingOntologies(closureModel, ontologyResource);
        if (!missingOntologies.isEmpty()) {
            if (enforceFullClosure) {
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
        classIris.addAll(model.filter(null, RDF.TYPE, OWL.CLASS).subjects());
        // Build our hierarchy index map.
        classIris.forEach(classResource -> {
            classIndex.put(classResource, generateInterface(classResource));
            // Add our class to the hierarchy
            classHierarchy.put(classResource, GraphUtils.lookupParentClasses(closureModel, classResource));
        });
        // Define the class hierarchy
        classHierarchy.forEach((classResource, parents) -> {
            try {
                JDefinedClass clazz = (JDefinedClass) classIndex.get(classResource);
                parents.forEach(parentResource -> {
                    JClass ref = findClassReference(parentResource)
                            //TODO - better error message
                            .orElseThrow(() -> new OrmGenerationException("Couldn't find parent class in index: " +
                                    parentResource));
                    clazz._implements(ref);
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
                property.getDomain().stream().map(classIndex::get)
                        .filter(JDefinedClass.class::isInstance)
                        .map(JDefinedClass.class::cast)
                        .forEach(property::attach));
        objectProperties.forEach((propResource, property) ->
                property.getDomain().stream().map(classIndex::get)
                        .filter(JDefinedClass.class::isInstance)
                        .map(JDefinedClass.class::cast)
                        .forEach(property::attach));
    }

    private JDefinedClass generateInterface(Resource resource)
            throws OrmException {
        try {
            // Create the interface in our package.
            JDefinedClass interfaze = jPackage._interface(JMod.PUBLIC,
                    NamingUtilities.getClassName(model, resource));
            interfaze._extends(Thing.class);
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
