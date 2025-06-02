package com.realmone.owl.orm;

import lombok.experimental.UtilityClass;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.InternedIRI;

/**
 * A utility class to create IRIs for use in generated interfaces so clients can do things like `Beer.TYPE` to get the
 * IRI of the Beer class or `Beer.ALCOHOLBYVOLUME` to get the IRI of the alcoholByVolume property.
 */
@UtilityClass
public class VocabularyIRIs {

    public static IRI createIRI(String namespace, String localName) {
        return new InternedIRI(namespace, localName);
    }
}
