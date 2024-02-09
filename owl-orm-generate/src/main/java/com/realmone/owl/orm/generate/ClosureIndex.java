package com.realmone.owl.orm.generate;

import com.sun.codemodel.JClass;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;

public interface ClosureIndex {

    Optional<JClass> findClassReference(Ontology generating, Resource classIri);
}
