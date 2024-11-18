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
package com.realmone.owl.orm.generate.support;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

@UtilityClass
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
