package com.realmone.owl.orm.generate.support;

import com.realmone.owl.orm.generate.OrmGenerationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.util.Set;
import java.util.stream.Collectors;

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

    public static Set<Resource> lookupParentClasses(Model model, Resource classResource) {
        return model.filter(classResource, RDFS.SUBCLASSOF, null)
                // Find the subClassOf properties
                .objects().stream().map(Resource.class::cast)
                .filter(parentResource -> {
                    if (model.filter(parentResource, null, null).isEmpty()) {
                        //TODO improve error message to include ontology, child class, and missing parent.
                        throw new OrmGenerationException("No ontology data about parent resource in model");
                    }
                    return !model.filter(parentResource, RDF.TYPE, OWL.CLASS).isEmpty();
                })
                //  collect into a set of resources.
                .collect(Collectors.toSet());
    }
}
