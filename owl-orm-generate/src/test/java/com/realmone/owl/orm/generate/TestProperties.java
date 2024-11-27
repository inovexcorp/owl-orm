/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
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
