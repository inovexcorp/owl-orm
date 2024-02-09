package com.realmone.owl.orm.generate.properties;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectProperty extends Property {

    JClass targetRange;

    @Builder(setterPrefix = "use")
    protected ObjectProperty(IRI rangeIri, JCodeModel codeModel, Resource resource,
                             String javaName, boolean functional) {
        super(codeModel, resource, javaName, functional, identifyRange(codeModel, rangeIri));
    }

    private static JClass identifyRange(JCodeModel codeModel, IRI rangeIri) {
        //TODO - figure this out!
        return null;
    }
}
