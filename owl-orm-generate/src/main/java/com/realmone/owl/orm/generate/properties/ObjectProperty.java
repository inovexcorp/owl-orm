package com.realmone.owl.orm.generate.properties;

import com.realmone.owl.orm.generate.ClosureIndex;
import com.realmone.owl.orm.generate.OrmGenerationException;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectProperty extends Property {

    @Builder(setterPrefix = "use")
    protected ObjectProperty(Resource rangeResource, Set<Resource> domains, ClosureIndex closureIndex,
                             JCodeModel codeModel, Resource resource, String javaName, boolean functional) {
        super(codeModel, resource, javaName, functional, identifyRange(closureIndex, rangeResource), closureIndex,
                domains, GraphUtils.printModelForJavadoc(closureIndex.findContext(resource)));
    }

    @Override
    public void additionalAttach(JDefinedClass jDefinedClass) throws OrmGenerationException {

    }

    private static JClass identifyRange(ClosureIndex closureIndex, Resource rangeIri) throws OrmGenerationException {
        return closureIndex.findClassReference(rangeIri)
                .orElseThrow(() -> new OrmGenerationException("Couldn't find class to reference for range: " +
                        rangeIri.stringValue()));
    }
}
