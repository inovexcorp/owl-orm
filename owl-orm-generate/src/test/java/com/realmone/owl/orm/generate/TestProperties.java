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

import com.realmone.owl.orm.generate.properties.DatatypeProperty;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class TestProperties {

    private static final ValueFactory VF = new ValidatingValueFactory();
    private static final ModelFactory MF = new DynamicModelFactory();

    @Test
    public void testDatatypeProperty() {
        JCodeModel codeModel = new JCodeModel();
        DatatypeProperty prop = DatatypeProperty.builder()
                .useJavaName("com.realmone.Fake")
                .useFunctional(true)
                .useCodeModel(codeModel)
                .useClosureIndex(new ClosureIndex() {
                    @Override
                    public Optional<JClass> findClassReference(Resource classIri) {
                        return Optional.empty();
                    }

                    @Override
                    public Model findContext(Resource resource) {
                        return MF.createEmptyModel();
                    }
                })
                .useRangeIri(XSD.STRING)
                .useResource(VF.createIRI("urn://fake"))
                .build();
        Assert.assertEquals("com.realmone.Fake", prop.getJavaName());
        Assert.assertEquals(String.class.getName(), prop.getTargetRange().fullName());
    }

    @Test(expected = NullPointerException.class)
    public void testDatatypePropBad() {
        JCodeModel codeModel = new JCodeModel();
        DatatypeProperty prop = DatatypeProperty.builder()
                .useJavaName("com.realmone.Fake")
                .useFunctional(true)
                .useCodeModel(null)
                .useRangeIri(XSD.STRING)
                .useResource(VF.createIRI("urn://fake"))
                .build();
    }
}
