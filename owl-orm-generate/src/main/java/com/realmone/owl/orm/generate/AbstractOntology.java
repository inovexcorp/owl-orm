package com.realmone.owl.orm.generate;

import com.sun.codemodel.JClass;
import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AbstractOntology implements ClosureIndex {

    protected static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    @Getter
    protected Map<Resource, JClass> classIndex = new HashMap<>();
    @Getter
    protected SourceGenerator sourceGenerator;
    @Getter
    protected Model closureModel = MODEL_FACTORY.createEmptyModel();

    @Override
    public Optional<JClass> findClassReference(Resource resource) {
        JClass ref = classIndex.get(resource);
        if (ref == null) {
            return sourceGenerator.getReferences().stream().map(refOnt -> refOnt.getClassIndex().get(resource))
                    .filter(Objects::nonNull).findFirst();
        } else {
            return Optional.of(ref);
        }
    }

    @Override
    public Model findContext(Resource resource) {
        return closureModel.filter(resource, null, null);
    }
}
