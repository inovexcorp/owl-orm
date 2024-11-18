package com.realmone.owl.orm.generate;

import com.sun.codemodel.JClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;

/**
 * Interface that describes a way to search across a closure of ontologies.
 */
public interface ClosureIndex {

    /**
     * Look for a class reference from within our closure of ontologies.
     *
     * @param classIri The IRI of the class in the ontology model
     * @return The {@link JClass} to use for code model references
     */
    Optional<JClass> findClassReference(Resource classIri);

    /**
     * Find the statements pertaining to a given resource within our closure of models.
     *
     * @param resource The {@link Resource} of the entity you're looking for
     * @return A {@link Model} containing the statements about your target resource
     */
    Model findContext(Resource resource);
}
