package com.realmone.owl.orm;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.InternedIRI;

/**
 * A utility class to create IRIs for use in generated interfaces so clients can do things like `Beer.TYPE` to get the
 * IRI of the Beer class or `Beer.ALCOHOLBYVOLUME` to get the IRI of the alcoholByVolume property.
 */
public class VocabularyIRIs {
    private VocabularyIRIs() {
    }

    public static IRI createIRI(String namespace, String localName) {
        return new InternedIRI(namespace, localName);
    }
}
