/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.maven;

import lombok.Getter;
import org.apache.maven.plugins.annotations.Parameter;

@Getter
public class Ontology {
    @Parameter(property = "ontologyFile", required = true)
    private String ontologyFile;
    @Parameter(property = "outputPackage", required = true)
    private String outputPackage;
    @Parameter(property = "ontologyName")
    private String ontologyName;
}
