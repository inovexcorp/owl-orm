/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate.properties;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.generate.ClosureIndex;
import com.realmone.owl.orm.generate.OrmGenerationException;
import com.realmone.owl.orm.generate.support.GraphUtils;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;
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
    public void additionalAttach(JDefinedClass jDefinedClass, String suffix) throws OrmGenerationException {
        addGetResourceMethod(jDefinedClass, suffix);
        addSetResourceMethod(jDefinedClass, suffix);
        if (!functional) {
            createAddRemoveMethod(jDefinedClass, true, suffix);
            createAddRemoveMethod(jDefinedClass, false, suffix);
        }
    }

    private static JClass identifyRange(ClosureIndex closureIndex, Resource rangeIri, JCodeModel codeModel)
            throws OrmGenerationException {
        return closureIndex.findClassReference(rangeIri)
                .orElseGet(() -> codeModel.ref(Thing.class));
    }

    private void addGetResourceMethod(JDefinedClass jDefinedClass, String suffix) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC,
                functional ? jCodeModel.ref(Optional.class).narrow(jCodeModel.ref(Resource.class))
                        : jCodeModel.ref(Set.class).narrow(jCodeModel.ref(Resource.class)),
        String.format("get%s%s_resource", javaName, suffix));
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();

        docs.add(functional ? String.format("<p>Get resource value for functional property <b>%s</b>.</p><br/>",
                resource.stringValue())
                : String.format("<p>Get resource values for non-functional property <b>%s</b>.</p><br/>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addReturn().add(functional ? "The optional resource value of the data from the underlying graph model."
                : "The set of resource values from the underlying graph model");
    }

    private void addSetResourceMethod(JDefinedClass jDefinedClass, String suffix) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC, jCodeModel.VOID, String.format("set%s%s_resource", javaName,
                suffix));
        JVar parameter = method.param(functional ? jCodeModel.ref(Resource.class) : jCodeModel.ref(Set.class)
                        .narrow(jCodeModel.ref(Resource.class)), functional ? "value" : "values");
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();
        docs.add(functional ? String.format("<p>Set the resource value for functional property <b>%s</b>.</p><br/>",
                resource.stringValue())
                : String.format("<p>Get resource values for non-functional property <b>%s</b>.</p><br/>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addParam(parameter).add(functional ? "The resource value to set the property to for this instance"
                : "The set of resource values to associate with this property for this instance");
    }

    private void createAddRemoveMethod(JDefinedClass jDefinedClass, boolean add, String suffix) {
        JMethod method = jDefinedClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, String.format("%s%s%s_resource", add ? "addTo"
                : "removeFrom", javaName, suffix));
        JVar parameter = method.param(jCodeModel.ref(Resource.class), add ? "toAdd" : "toRemove");
        annotateMethod(method, targetRange.dotclass());
        JDocComment docs = method.javadoc();
        docs.add(add ? String.format("<p>Add a resource value to the set underneath non-functional property <b>%s</b>.</p><br>",
                resource.stringValue())
                : String.format("<p>Remove a resource value from the set underneath non-functional property <b>%s</b>.</p><br>",
                resource.stringValue()));
        docs.add(commentContext);
        docs.addParam(parameter).add(add ? "The resource value to add to the property to for this instance"
                : "The resource value to remove from the property to for this instance");
        docs.addReturn().add(add ? "Whether the new resource value was added to the set of data"
                : "Whether the resource value was removed from the set of data");
    }
}
