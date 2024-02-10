package com.realmone.owl.orm.generate.properties;

import com.realmone.owl.orm.generate.ClosureIndex;
import com.realmone.owl.orm.generate.OrmGenerationException;
import com.sun.codemodel.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        // Build methods...
        createGetter(jDefinedClass);
        createSetter(jDefinedClass);
        // addTo, removeFrom, clearOut on non-functional fields
        if (!functional) {
            createAddRemoveMethod(jDefinedClass, true);
            createAddRemoveMethod(jDefinedClass, false);
            createClearOutMethod(jDefinedClass);
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
        docs.addReturn().add(add ? "Whether or not the new value was added to the set of data"
                : "Whether or not the value was removed from the set of data");
    }

    public void createClearOutMethod(JDefinedClass jDefinedClass) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, String.format("clearOut%s", javaName));
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();
        docs.add(String.format("<p>Clear out all values associated with property <b>%s</b>.</p><br>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addReturn().add("Whether or not elements were cleared out");
    }

    private void annotateMethod(JMethod method, JExpression rangeClass) {
        method.annotate(jCodeModel.ref(com.realmone.owl.orm.annotations.Property.class))
                .param("value", resource.stringValue())
                .param("functional", functional)
                .param("type", rangeClass);
    }
}
