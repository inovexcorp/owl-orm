/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
