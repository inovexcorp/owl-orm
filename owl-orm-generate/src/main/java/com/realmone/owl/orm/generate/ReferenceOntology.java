package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.generate.properties.DatatypeProperty;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.realmone.owl.orm.generate.support.NamingUtilities;
import com.sun.codemodel.JCodeModel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ReferenceOntology {

    @Getter
    private final Resource ontologyResource;
    @Getter
    private final Model model;
    @Getter
    private final String packageName;
    @Getter
    private final String ontologyName;
    @Getter
    private final Map<Resource, String> classIndex = new HashMap<>();
    @Getter
    private final Map<Resource, DatatypeProperty> datatypeProperties = new HashMap<>();


    @Builder
    protected ReferenceOntology(@NonNull JCodeModel codeModel, @NonNull Model ontologyModel,
                                @NonNull String packageName, @NonNull String ontologyName) throws OrmException {
        final Set<Resource> ontologiesInModel = ontologyModel.filter(null, RDF.TYPE, OWL.ONTOLOGY).subjects();
        if (ontologiesInModel.size() > 1) {
            throw new OrmException("Ontology data contains multiple ontology definitions");
        } else if (ontologiesInModel.isEmpty()) {
            throw new OrmException("Ontology data contains no ontology definition");
        } else {
            this.ontologyResource = ontologiesInModel.stream().findFirst().orElseThrow();
            this.model = ontologyModel;
            this.packageName = packageName;
            this.ontologyName = ontologyName;
            this.model.filter(null, RDF.TYPE, OWL.CLASS).subjects().forEach(resource ->
                    classIndex.put(resource, String.format("%s.%s", packageName,
                            NamingUtilities.getClassName(ontologyModel, resource))));
            this.model.filter(null, RDF.TYPE, OWL.DATATYPEPROPERTY).subjects()
                    .forEach(propResource -> datatypeProperties.put(propResource,
                            DatatypeProperty.builder()
                                    .useResource(propResource)
                                    .useFunctional(GraphUtils.lookupFunctional(ontologyModel, propResource))
                                    .useCodeModel(codeModel)
                                    .useJavaName(NamingUtilities.getPropertyName(ontologyModel, propResource))
                                    .useRangeIri(GraphUtils.lookupRange(ontologyModel, propResource))
                                    .build()));
        }
    }

    public boolean containsClass(final Resource classIri) {
        return classIndex.containsKey(classIri);
    }

    public Optional<String> getClassName(final Resource classIri) {
        return Optional.ofNullable(classIndex.get(classIri));
    }

    public boolean containsProperty(final Resource propertyIri) {
        return !(model.filter(propertyIri, RDF.TYPE, RDF.PROPERTY).isEmpty()
                && model.filter(propertyIri, RDF.TYPE, OWL.DATATYPEPROPERTY).isEmpty()
                && model.filter(propertyIri, RDF.TYPE, OWL.OBJECTPROPERTY).isEmpty());
    }

    public Optional<String> getPropertyName(final Resource propertyName) {
        return Optional.ofNullable(
                containsProperty(propertyName) ? NamingUtilities.getPropertyName(model, propertyName) : null);
    }
    // TODO initialize Property instances?
}
