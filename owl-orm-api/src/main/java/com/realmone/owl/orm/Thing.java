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
package com.realmone.owl.orm;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.Optional;
import java.util.Set;

/**
 * A class that represents a owl Thing. Basically a base structure for all things in the
 * owl ontology universe to extend.
 */
public interface Thing {

    /**
     * @return The {@link Resource} identifying this {@link Thing}
     */
    Resource getResource();

    /**
     * @return The backing {@link Model} for this {@link Thing}
     */
    Model getModel();

    /**
     * @return The list of parent class IRIs for the type of this {@link Thing}
     */
    Set<IRI> getParents();

    /**
     * Get a {@link Value} from the given predicate {@link IRI}.
     *
     * @param predicate The predicate identifying the property
     * @param context   The context {@link IRI}
     * @return The {@link Value} from the backing {@link Model} for this
     * {@link Thing}
     */
    Optional<Value> getProperty(IRI predicate, IRI... context);

    /**
     * Get a non-functional {@link Set} of {@link Value} objects from the
     * backing {@link Model}.
     *
     * @param predicate The {@link IRI} of the predicate you want values for
     * @param context   The {@link IRI} of the context of this statement
     * @return The {@link Set} of {@link Value}s with the specified prediciate
     * for this object
     */
    Set<Value> getProperties(IRI predicate, IRI... context);

    /**
     * Set a property in the backing model for this {@link Thing}. Removes other
     * statements with the same subject/predicate/context.
     *
     * @param value     The {@link Value} to store
     * @param predicate The predicate {@link IRI} of the property to set
     * @param context   The {@link IRI} of the context
     * @return Whether or not the property was set
     */
    boolean setProperty(Value value, IRI predicate, IRI... context);

    /**
     * Set the values of a non-functional property. Removes other statements
     * with the same subject/predicate/context.
     *
     * @param value     The {@link Set} of {@link Value}s to set the property to
     * @param predicate The prediciate {@link IRI} of the property to set
     * @param context   The context {@link IRI} to set the values for
     */
    void setProperties(Set<Value> value, IRI predicate, IRI... context);

    /**
     * Add a {@link Value} to a non-functional property for this Thing.
     *
     * @param value     The {@link Value} to add
     * @param predicate The predicate {@link IRI} to add it to
     * @param context   The context {@link IRI} to use
     * @return Whether or not the {@link Value} was added to
     */
    boolean addProperty(Value value, IRI predicate, IRI... context);

    /**
     * Remove the specified {@link Value} from the given non-functional property
     * for this {@link Thing}.
     *
     * @param value     The value to remove
     * @param predicate The predicate {@link IRI} to remove the {@link Value} from
     * @param context   The context {@link IRI} to use
     * @return Whether or not the {@link Value} was removed
     */
    boolean removeProperty(Value value, IRI predicate, IRI... context);

    /**
     * Clear out the values associated with the given predicate in this {@link Thing}.
     *
     * @param predicate The {@link IRI} of the predicate to clear out
     * @param context   The {@link IRI} contexts to remove with
     * @return Whether or not data was removed from this {@link Thing}
     */
    boolean clearProperty(IRI predicate, IRI... context);

    /**
     * @return The IRI that identifies instances of this type.
     */
    IRI getTypeIri();
}
