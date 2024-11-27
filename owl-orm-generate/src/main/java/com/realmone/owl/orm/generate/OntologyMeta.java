/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.generate;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OntologyMeta {
    private String file;
    private String packageName;
    private String ontologyName;
}
