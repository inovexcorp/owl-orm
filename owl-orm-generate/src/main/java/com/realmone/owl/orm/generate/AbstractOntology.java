package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.Thing;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AbstractOntology implements ClosureIndex {

    protected static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    @Getter
    protected final Map<Resource, JClass> classIndex = new HashMap<>();
    @Getter
    protected final Model closureModel = MODEL_FACTORY.createEmptyModel();
    @Getter
    protected final SourceGenerator sourceGenerator;
    @Getter
    protected final JCodeModel codeModel;

    public AbstractOntology(SourceGenerator sourceGenerator, JCodeModel codeModel) {
        this.sourceGenerator = sourceGenerator;
        this.codeModel = codeModel;
    }

    @Override
    public Optional<JClass> findClassReference(Resource resource) {
        JClass ref = classIndex.get(resource);
        if (ref == null && resource.equals(OWL.THING)) {
            return Optional.of(codeModel.ref(Thing.class));
        } else if (ref == null) {
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
