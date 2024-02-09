package com.realmone.owl.orm.generate.support;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public class NamingUtilities {

    public static String getClassName(Model closure, Resource resource) {
        return safeName(getNameTemplate(closure, resource), true);
    }

    public static String getPropertyName(Model closure, Resource resource) {
        return safeName(getNameTemplate(closure, resource), true);
    }

    public static String getNameTemplate(Model closure, Resource resource) {
        // DCTERMS:title, RDFS:label, then IRI localname
        return closure.filter(resource, DCTERMS.TITLE, null).objects().stream().findFirst().map(Value::stringValue)
                .orElseGet(() ->
                        closure.filter(resource, RDFS.LABEL, null).objects().stream().findFirst()
                                .map(Value::stringValue)
                                .orElseGet(() ->
                                        (resource.isIRI()) ? ((IRI) resource).getLocalName() : resource.stringValue()));
    }

    /**
     * Simple method to strip whitespaces from the name. It will also ensure it
     * is a valid class or field name.
     *
     * @param input The input string
     * @return The stripped and cleaned output name
     */
    public static String safeName(final String input, boolean capitalizeFirst) {
        StringBuilder builder = new StringBuilder();
        boolean lastIsWhiteSpace = false;
        boolean first = true;
        for (char c : input.toCharArray()) {
            if (first && !Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(c)) {
                builder.append("_");
                builder.append(capitalizeFirst ? Character.toUpperCase(c) : c);
                first = false;
            } else if (Character.isWhitespace(c)) {
                lastIsWhiteSpace = true;
            } else if (Character.isJavaIdentifierPart(c)) {
                builder.append(lastIsWhiteSpace || (first && capitalizeFirst) ? StringUtils.capitalize(c + "") : c);
                lastIsWhiteSpace = false;
                first = false;
            }
        }
        return builder.toString();
    }
}
