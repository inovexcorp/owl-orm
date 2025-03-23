package com.realmone.owl.orm;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.InternedIRI;

public class VocabularyIRIs {
    private VocabularyIRIs() {
    }

    public static IRI createIRI(String namespace, String localName) {
        return new InternedIRI(namespace, localName);
    }
}
