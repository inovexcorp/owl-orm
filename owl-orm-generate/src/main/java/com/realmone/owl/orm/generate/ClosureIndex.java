package com.realmone.owl.orm.generate;

import com.sun.codemodel.JClass;
import org.eclipse.rdf4j.model.Resource;

import java.util.Optional;

public interface ClosureIndex {

    Optional<JClass> findClassReference(GeneratingOntology generating, Resource classIri);
}
