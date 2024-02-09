package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.generate.properties.DatatypeProperty;
import com.sun.codemodel.JCodeModel;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.Assert;
import org.junit.Test;

public class TestProperties {

    private static final ValueFactory VF = new ValidatingValueFactory();

    @Test
    public void testDatatypeProperty() {
        JCodeModel codeModel = new JCodeModel();
        DatatypeProperty prop = DatatypeProperty.builder()
                .useJavaName("com.realmone.Fake")
                .useFunctional(true)
                .useCodeModel(codeModel)
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
