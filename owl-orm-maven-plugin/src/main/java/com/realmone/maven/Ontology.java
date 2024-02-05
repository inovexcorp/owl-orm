package com.realmone.maven;

import com.realmone.owl.orm.OrmException;
import com.sun.codemodel.JDefinedClass;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Ontology {

    @Getter
    @Parameter(property = "ontologyFile", required = true)
    private String ontologyFile;

    @Getter
    @Parameter(property = "outputPackage", required = true)
    private String outputPackage;

    @Getter
    @Parameter(property = "ontologyName")
    private String ontologyName;

    @Getter
    private Resource resource;
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
    private Model model;

    /*
     * The following fields are used to track and build the JCodeModel as we go.
     */

    @Getter
    private final Map<Resource, JDefinedClass> resourceClassMap = new HashMap<>();

    public Model loadModel(@NonNull FileSystemManager fileSystemManager) throws OrmException {
        try (FileObject fileObject = fileSystemManager.resolveFile(ontologyFile);
             final InputStream is = fileObject.getContent().getInputStream()) {
            final Optional<RDFFormat> format = Rio.getParserFormatForFileName(fileObject.getName().getBaseName());
            if (format.isPresent()) {
                this.model = Rio.parse(is, format.get());
                analyze(model);
                return model;
            } else {
                throw new OrmException("Could not identify format of file containing ontology: " + fileObject.getName());
            }
        } catch (IOException e) {
            throw new OrmException("Issue loading ontology model data", e);
        }
    }

    private void analyze(Model model) throws OrmException {
        final Set<Resource> ontologies = model.filter(null, RDF.TYPE, OWL.ONTOLOGY).subjects();
        if (ontologies.size() > 1) {
            throw new OrmException(String.format("More than one ontology in file '%s': %s", ontologyFile, ontologies));
        } else if (ontologies.isEmpty()) {
            throw new OrmException(String.format("No ontology defined in file '%s'", ontologyFile));
        } else {
            // For each ontology in the data -- should be exactly one.
            ontologies.forEach(ontologyResource -> {
                // Put in our index the resource of that ontology
                model.filter(ontologyResource, OWL.IMPORTS, null).objects().stream()
                        // Convert from Value to Resource and add to our set.
                        .map(this::toResource).forEach(imports::add);
                this.resource = ontologyResource;
            });
            classes.addAll(model.filter(null, RDF.TYPE, OWL.CLASS).subjects());
            classes.forEach(clazz ->
                    // Add our class to the hierarchy
                    classHierarchy.put(clazz, model.filter(clazz, RDFS.SUBCLASSOF, null)
                            // Find the subClassOf properties, and collect into a set of resources.
                            .objects().stream().map(val -> (Resource) val).collect(Collectors.toSet()))

            );
            //TODO - RDFS.CLASS support?
            datatypeProperties.addAll(model.filter(null, RDF.TYPE, OWL.DATATYPEPROPERTY).subjects());
            objectProperties.addAll(model.filter(null, RDF.TYPE, OWL.OBJECTPROPERTY).subjects());
        }
    }

    private Resource toResource(Value value) {
        return (Resource) value;
    }
}
