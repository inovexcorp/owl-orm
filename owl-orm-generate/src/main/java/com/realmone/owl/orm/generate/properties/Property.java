/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate.properties;

import com.realmone.owl.orm.VocabularyIRIs;
import com.realmone.owl.orm.generate.ClosureIndex;
import com.realmone.owl.orm.generate.OrmGenerationException;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Property {

    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    protected JCodeModel jCodeModel;
    protected Resource resource;
    protected String javaName;
    protected boolean functional;
    protected JClass targetRange;
    protected ClosureIndex closureIndex;
    protected Set<Resource> domain;
    protected String commentContext;

    protected abstract void additionalAttach(JDefinedClass jDefinedClass) throws OrmGenerationException;

    public void attach(JDefinedClass jDefinedClass) throws OrmGenerationException {
        // Add IRI field
        if (resource.isIRI()) {
            IRI propIRI = (IRI) resource;
            String staticFieldName = javaName.toUpperCase();
            if (jDefinedClass.fields().containsKey(staticFieldName)) {
                staticFieldName += "1";
            }
            jDefinedClass.field(JMod.STATIC, IRI.class, staticFieldName, jCodeModel.ref(VocabularyIRIs.class).staticInvoke("createIRI")
                            .arg(propIRI.getNamespace())
                            .arg(propIRI.getLocalName()))
                    .javadoc().add("The IRI value of the " + resource + " property");
        }
        // Build methods...
        createGetter(jDefinedClass);
        createSetter(jDefinedClass);
        createClearOutMethod(jDefinedClass);
        // addTo, removeFrom, clearOut on non-functional fields
        if (!functional) {
            createAddRemoveMethod(jDefinedClass, true);
            createAddRemoveMethod(jDefinedClass, false);
//            createClearOutMethod(jDefinedClass);
        }
        additionalAttach(jDefinedClass);
    }

    private void createGetter(JDefinedClass jDefinedClass) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC,
                functional ? jCodeModel.ref(Optional.class).narrow(targetRange)
                        : jCodeModel.ref(Set.class).narrow(targetRange),
                String.format("%s%s", functional && targetRange.fullName().equals(Boolean.class.getName()) ? "is"
                        : "get", javaName));
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();

        docs.add(functional ? String.format("<p>Get value for functional property <b>%s</b>.</p><br/>",
                resource.stringValue())
                : String.format("<p>Get values for non-functional property <b>%s</b>.</p><br/>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addReturn().add(functional ? "The optional value of the data from the underlying graph model."
                : "The set of values from the underlying graph model");
    }

    private void createSetter(JDefinedClass jDefinedClass) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC, jCodeModel.VOID, String.format("set%s", javaName));
        JVar parameter = method.param(functional ? targetRange : jCodeModel.ref(Set.class).narrow(targetRange),
                functional ? "value" : "values");
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();
        docs.add(functional ? String.format("<p>Set the value for functional property <b>%s</b>.</p><br/>",
                resource.stringValue())
                : String.format("<p>Get values for non-functional property <b>%s</b>.</p><br/>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addParam(parameter).add(functional ? "The value to set the property to for this instance"
                : "The set of values to associate with this property for this instance");
    }

    private void createAddRemoveMethod(JDefinedClass jDefinedClass, boolean add) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, String.format("%s%s", add ? "addTo"
                : "removeFrom", javaName));
        JVar parameter = method.param(targetRange, add ? "toAdd" : "toRemove");
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();
        docs.add(add ? String.format("<p>Add a value to the set underneath non-functional property <b>%s</b>.</p><br>",
                resource.stringValue())
                : String.format("<p>Remove a value from the set underneath non-functional property <b>%s</b>.</p><br>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addParam(parameter).add(add ? "The value to add to the property to for this instance"
                : "The value to remove from the property to for this instance");
        docs.addReturn().add(add ? "Whether the new value was added to the set of data"
                : "Whether the value was removed from the set of data");
    }

    public void createClearOutMethod(JDefinedClass jDefinedClass) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, String.format("clearOut%s", javaName));
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();
        docs.add(String.format("<p>Clear out all values associated with property <b>%s</b>.</p><br>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addReturn().add("Whether elements were cleared out");
    }

    private void annotateMethod(JMethod method, JExpression rangeClass) {
        method.annotate(jCodeModel.ref(com.realmone.owl.orm.annotations.Property.class))
                .param("value", resource.stringValue())
                .param("functional", functional)
                .param("type", rangeClass);
    }
}
