/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate.support;

import com.google.common.xml.XmlEscapers;
import com.realmone.owl.orm.generate.OrmGenerationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class GraphUtils {

    public static boolean lookupFunctional(Model model, Resource resource) {
        return !(model.filter(resource, RDF.TYPE, OWL.FUNCTIONALPROPERTY).isEmpty());
    }

    public static Resource lookupRange(Model model, Resource resource) {
        Set<Value> values = model.filter(resource, RDFS.RANGE, null).objects();
        // If there is more than one range specified.
        if (values.size() > 1) {
            return RDF.VALUE;
        }
        // Else do a lookup based upon the
        else {
            // Grab the first matching Range...
            return values.stream().map(Resource.class::cast).findFirst()
                    // Default to RDF.VALUE if no range is specified...
                    .orElseGet(() -> RDF.VALUE);
        }
    }

    public static Set<Resource> lookupDomain(Model model, Resource propertyResource) throws OrmGenerationException {
        try {
            //TODO - handle mutli-domain better... Shouldn't just be a list, but an intersection/union construct.
            return model.filter(propertyResource, RDFS.DOMAIN, null).objects().stream().map(Resource.class::cast)
                    .collect(Collectors.toSet());
        } catch (ClassCastException e) {
            throw new OrmGenerationException("Issue looking up domains for property: " +
                    propertyResource.stringValue(), e);
        }
    }

    public static Set<Resource> lookupParentClasses(Model model, Resource classResource, boolean enforceFullClosure)
            throws OrmGenerationException {
        try {
            return model.filter(classResource, RDFS.SUBCLASSOF, null)
                    // Find the subClassOf properties
                    .objects().stream().map(Resource.class::cast)
                    .filter(Resource::isIRI) // Ignore blank nodes (usually restrictions)
                    .filter(parentResource -> {
                        if (model.filter(parentResource, null, null).isEmpty()) {
                            String message = String.format("Searching for a parent of class %s, and couldn't find the parent class of %s",
                                    classResource.stringValue(), parentResource.stringValue());
                            if (enforceFullClosure) {
                                throw new OrmGenerationException(message);
                            } else {
                                log.warn(message);
                            }
                        }
                        return !model.filter(parentResource, RDF.TYPE, OWL.CLASS).isEmpty();
                    })
                    //  collect into a set of resources.
                    .collect(Collectors.toSet());
        } catch (ClassCastException e) {
            throw new OrmGenerationException("Issue getting parent classes where a parent class wasn't a resource", e);
        }
    }

    public static Set<Resource> getImports(Model model, Resource ontology) {
        try {
            return model.filter(ontology, OWL.IMPORTS, null).objects().stream()
                    // Map to resources
                    .map(Resource.class::cast)
                    // Collect as a set.
                    .collect(Collectors.toSet());
        } catch (ClassCastException e) {
            throw new OrmGenerationException("Problem getting imports for ontology: " + ontology.stringValue());
        }
    }

    public static Set<Resource> missingOntologies(Model model, Resource centralOntology) {
        Set<Resource> missing = new HashSet<>();
        checkForMissingOntologies(missing, model, getImports(model, centralOntology), new HashSet<>());
        return missing;
    }

    public static String printModelForJavadoc(Model model) {
        try (Writer writer = new StringWriter()) {
            Rio.write(model, writer, RDFFormat.TURTLE);
            return String.format("<p><i>%s</i></p>",
                    XmlEscapers.xmlContentEscaper().escape(writer.toString())
                            .replace("\n", "<br>\n"));
        } catch (IOException e) {
            //TODO - better error handling...
            throw new OrmGenerationException("", e);
        }
    }

    private static void checkForMissingOntologies(Set<Resource> missing, Model model, Set<Resource> lookingFor, Set<Resource> seen) {
        seen.addAll(lookingFor);
        Set<Resource> alsoLookFor = new HashSet<>();
        lookingFor.forEach(importResource -> {
            if (model.filter(importResource, RDF.TYPE, OWL.ONTOLOGY).isEmpty()) {
                missing.add(importResource);
            } else {
                Set<Resource> imports = getImports(model, importResource);
                imports.removeAll(seen);
                alsoLookFor.addAll(imports);
            }
        });
        if (!alsoLookFor.isEmpty()) {
            checkForMissingOntologies(missing, model, alsoLookFor, seen);
        }
    }
}
