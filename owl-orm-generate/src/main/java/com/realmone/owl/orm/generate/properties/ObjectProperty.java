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
package com.realmone.owl.orm.generate.properties;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.generate.ClosureIndex;
import com.realmone.owl.orm.generate.OrmGenerationException;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.rdf4j.model.Resource;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectProperty extends Property {

    @Builder(setterPrefix = "use")
    protected ObjectProperty(Resource rangeResource, Set<Resource> domains, ClosureIndex closureIndex,
                             JCodeModel codeModel, Resource resource, String javaName, boolean functional) {
        super(codeModel, resource, javaName, functional, identifyRange(closureIndex, rangeResource, codeModel),
                closureIndex, domains, GraphUtils.printModelForJavadoc(closureIndex.findContext(resource)));
    }

    @Override
    public void additionalAttach(JDefinedClass jDefinedClass) throws OrmGenerationException {

    }

    private static JClass identifyRange(ClosureIndex closureIndex, Resource rangeIri, JCodeModel codeModel)
            throws OrmGenerationException {
        return closureIndex.findClassReference(rangeIri)
                .orElseGet(() -> codeModel.ref(Thing.class));
    }
}
