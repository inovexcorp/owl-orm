/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate.properties;

import com.realmone.owl.orm.generate.ClosureIndex;
import com.realmone.owl.orm.generate.OrmGenerationException;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatatypeProperty extends Property {

    @Builder(setterPrefix = "use")
    protected DatatypeProperty(Resource rangeIri, Set<Resource> domains, JCodeModel codeModel, ClosureIndex closureIndex,
                               Resource resource, String javaName, boolean functional) {
        super(codeModel, resource, javaName, functional, identifyRange(codeModel, rangeIri), closureIndex, domains,
                GraphUtils.printModelForJavadoc(closureIndex.findContext(resource)));
    }

    @Override
    public void additionalAttach(JDefinedClass jDefinedClass) throws OrmGenerationException {


    }

    private static JClass identifyRange(JCodeModel codeModel, Resource rangeIri) {
        if (rangeIri.equals(RDFS.LITERAL)) {
            return codeModel.ref(Literal.class);
        } else if (rangeIri.equals(XSD.ANYURI)) {
            return codeModel.ref(IRI.class);
        } else if (rangeIri.equals(RDFS.RESOURCE) || rangeIri.equals(RDF.VALUE)) {
            return codeModel.ref(Value.class);
        } else if (rangeIri.equals(XSD.STRING)) {
            return codeModel.ref(String.class);
        } else if (rangeIri.equals(XSD.BOOLEAN)) {
            return codeModel.ref(Boolean.class);
        } else if (rangeIri.equals(XSD.BYTE)) {
            return codeModel.ref(Byte.class);
        } else if (rangeIri.equals(XSD.DATE) || rangeIri.equals(XSD.DATETIME)) {
            return codeModel.ref(OffsetDateTime.class);
        } else if (rangeIri.equals(XSD.FLOAT)) {
            return codeModel.ref(Float.class);
        } else if (rangeIri.equals(XSD.DOUBLE)) {
            return codeModel.ref(Double.class);
        } else if (rangeIri.equals(XSD.LONG)) {
            return codeModel.ref(Long.class);
        } else if (rangeIri.equals(XSD.INTEGER)) {
            return codeModel.ref(Integer.class);
        } else {
            return codeModel.ref(Value.class);
        }
    }
}
