package com.realmone.owl.orm.generate.properties;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
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

@Data
@EqualsAndHashCode(callSuper = true)
public class DatatypeProperty extends Property {

    @Builder(setterPrefix = "use")
    protected DatatypeProperty(Resource rangeIri, JCodeModel codeModel, Resource resource,
                               String javaName, boolean functional) {
        super(codeModel, resource, javaName, functional, identifyRange(codeModel, rangeIri));
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
