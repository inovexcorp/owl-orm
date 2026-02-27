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
import com.sun.codemodel.JExpression;
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

    private static final String RESOURCE_SUFFIX = "_Resource";

    @Builder(setterPrefix = "use")
    protected ObjectProperty(Resource rangeResource, Set<Resource> domains, ClosureIndex closureIndex,
                             JCodeModel codeModel, Resource resource, String javaName, boolean functional) {
        super(codeModel, resource, javaName, functional, identifyRange(closureIndex, rangeResource, codeModel),
                closureIndex, domains, GraphUtils.printModelForJavadoc(closureIndex.findContext(resource)));
    }

    @Override
    public void additionalAttach(JDefinedClass jDefinedClass, String suffix) throws OrmGenerationException {
        JClass resourceType = jCodeModel.ref(Resource.class);
        JExpression resourceDotClass = resourceType.dotclass();

        // Getter: getXxx_Resource() -> Optional<Resource> or Set<Resource>
        JMethod getter = jDefinedClass.method(JMod.PUBLIC,
                functional ? jCodeModel.ref(Optional.class).narrow(resourceType)
                        : jCodeModel.ref(Set.class).narrow(resourceType),
                String.format("get%s%s%s", javaName, suffix, RESOURCE_SUFFIX));
        annotateResourceMethod(getter, resourceDotClass);
        JDocComment getterDocs = getter.javadoc();
        getterDocs.add(String.format("<p>Get %s Resource IRI(s) for property <b>%s</b>.</p><br/>",
                functional ? "value" : "values", resource.stringValue()));
        getterDocs.add(commentContext);
        getterDocs.addReturn().add(functional ? "The optional Resource IRI from the underlying graph model."
                : "The set of Resource IRIs from the underlying graph model");

        // Setter: setXxx_Resource(Resource) or setXxx_Resource(Set<Resource>)
        JMethod setter = jDefinedClass.method(JMod.PUBLIC, jCodeModel.VOID,
                String.format("set%s%s%s", javaName, suffix, RESOURCE_SUFFIX));
        JVar setterParam = setter.param(
                functional ? resourceType : jCodeModel.ref(Set.class).narrow(resourceType),
                functional ? "value" : "values");
        annotateResourceMethod(setter, resourceDotClass);
        JDocComment setterDocs = setter.javadoc();
        setterDocs.add(String.format("<p>Set %s Resource IRI(s) for property <b>%s</b>.</p><br/>",
                functional ? "value" : "values", resource.stringValue()));
        setterDocs.add(commentContext);
        setterDocs.addParam(setterParam).add(functional ? "The Resource IRI to set for this instance"
                : "The set of Resource IRIs to associate with this property for this instance");

        // addTo/removeFrom only for non-functional properties
        if (!functional) {
            // AddTo: addToXxx_Resource(Resource) -> boolean
            JMethod addTo = jDefinedClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN,
                    String.format("addTo%s%s%s", javaName, suffix, RESOURCE_SUFFIX));
            JVar addParam = addTo.param(resourceType, "toAdd");
            annotateResourceMethod(addTo, resourceDotClass);
            JDocComment addDocs = addTo.javadoc();
            addDocs.add(String.format("<p>Add a Resource IRI to the set underneath non-functional property <b>%s</b>.</p><br>",
                    resource.stringValue()));
            addDocs.add(commentContext);
            addDocs.addParam(addParam).add("The Resource IRI to add");
            addDocs.addReturn().add("Whether the new value was added to the set of data");

            // RemoveFrom: removeFromXxx_Resource(Resource) -> boolean
            JMethod removeFrom = jDefinedClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN,
                    String.format("removeFrom%s%s%s", javaName, suffix, RESOURCE_SUFFIX));
            JVar removeParam = removeFrom.param(resourceType, "toRemove");
            annotateResourceMethod(removeFrom, resourceDotClass);
            JDocComment removeDocs = removeFrom.javadoc();
            removeDocs.add(String.format("<p>Remove a Resource IRI from the set underneath non-functional property <b>%s</b>.</p><br>",
                    resource.stringValue()));
            removeDocs.add(commentContext);
            removeDocs.addParam(removeParam).add("The Resource IRI to remove");
            removeDocs.addReturn().add("Whether the value was removed from the set of data");
        }
    }

    private void annotateResourceMethod(JMethod method, JExpression resourceDotClass) {
        method.annotate(jCodeModel.ref(com.realmone.owl.orm.annotations.Property.class))
                .param("value", resource.stringValue())
                .param("functional", functional)
                .param("type", resourceDotClass);
    }

    private static JClass identifyRange(ClosureIndex closureIndex, Resource rangeIri, JCodeModel codeModel)
            throws OrmGenerationException {
        return closureIndex.findClassReference(rangeIri)
                .orElseGet(() -> codeModel.ref(Thing.class));
    }
}
