package com.realmone.maven;

import com.realmone.owl.orm.OrmException;
import com.realmone.owl.orm.generate.OntologyMeta;
import com.sun.codemodel.JDefinedClass;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Ontology {
    @Parameter(property = "ontologyFile", required = true)
    private String ontologyFile;
    @Parameter(property = "outputPackage", required = true)
    private String outputPackage;
    @Parameter(property = "ontologyName")
    private String ontologyName;
}
