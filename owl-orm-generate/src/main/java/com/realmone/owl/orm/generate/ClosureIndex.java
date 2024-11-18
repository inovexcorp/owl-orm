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
