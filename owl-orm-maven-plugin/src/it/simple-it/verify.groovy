/**
 * Groovy test for maven generation plugin execution.
 */
import groovy.xml.XmlSlurper
import org.eclipse.rdf4j.model.BNode
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.DC
import org.eclipse.rdf4j.model.vocabulary.DCTERMS
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.RDFFormat

// Path to the POM file
File pomFile = new File(basedir, "pom.xml")
assert pomFile.exists() : "POM file does not exist: ${pomFile.absolutePath}"

// Parse the POM file
def pom = new XmlSlurper().parse(pomFile)

// Locate ontologies in the plugin configuration
def ontologies = pom.build.plugins.plugin.findAll {
    it.artifactId.text().equals("owl-orm-maven-plugin") && it.groupId.text().equals("com.realmone")
}.configuration.generates.ontology

assert ontologies.size() > 0 : "No ontologies found in the plugin configuration"

// Path to the generated sources directory
File outputDir = new File(basedir, "target/generated-sources")
assert outputDir.exists() : "Output directory does not exist: ${outputDir.absolutePath}"

// Map each ontology to its expected package and validate sources
ontologies.each { ontology ->
    print("Loading ontoloy ${ontology}")
    def ontologyFile = new File(ontology.ontologyFile.text().replace('${project.basedir}', basedir.absolutePath))
    def outputPackage = ontology.outputPackage.text()
    def ontologyName = ontology.ontologyName.text()

    assert ontologyFile.exists() : "Source ontology file does not exist: ${ontologyFile.absolutePath}"

    // Transform package name to directory structure
    def expectedPackageDir = outputPackage.replace('.', '/')
    def packageDir = new File(outputDir, expectedPackageDir)
    assert packageDir.exists() : "Expected generated package directory does not exist: ${packageDir.absolutePath}"

    // Verify the expected Thing superclass
    def thingFile = new File(packageDir, "${ontologyName.replaceAll('[^a-zA-Z0-9]', '')}Thing.java")
    // Wait for a file to exist for up to 20 seconds
    int waited = 0
    while (!thingFile.exists() && waited < 20) {
        println "Waiting for file to exist: ${thingFile.absolutePath}"
        Thread.sleep(1000) // Wait 1 second
        waited++
    }
    assert thingFile.exists() : "Expected Thing superclass file does not exist: ${thingFile.absolutePath}"

    // Parse the ontology file using RDF4J's Rio
    def rdfModel
    ontologyFile.withInputStream { inputStream ->
        def format = Rio.getParserFormatForFileName(ontologyFile.toURI().toString())
                .orElseThrow(() -> new IllegalArgumentException("Couldn't get type for file" + ontologyFile.absolutePath));
        rdfModel = Rio.parse(inputStream, ontologyFile.toURI().toString(), format)

    }
    assert rdfModel != null : "Issue reading data from ontology file: " + ontologyFile.absolutePath

    // Extract OWL classes and their dc:title values
    def classes = rdfModel.filter(null, RDF.TYPE, OWL.CLASS).subjects().collect { subject ->
        def title = rdfModel.filter(subject, DCTERMS.TITLE, null).objects().find()?.stringValue()
        def label = rdfModel.filter(subject, RDFS.LABEL, null).objects().find()?.stringValue()
        if (title) {
            new Tuple(subject, title.replaceAll('[^a-zA-Z0-9]', ''))
        } else if (label) {
            new Tuple(subject, label.replaceAll('[^a-zA-Z0-9]', ''))
        } else {
            new Tuple(subject, subject.localName.replaceAll('[^a-zA-Z0-9]', ''))
        }
    }

    println classes
    // Verify ontology-specific classes are generated
    classes.each { iri, className ->
        println "Checking for generated source: ${className}.java"
        def classFile = new File(packageDir, "${className}.java")
        assert classFile.exists(): "Expected class file for ${className} does not exist: ${classFile.absolutePath}"

        println "Checking for @Type annotation on ${classFile}"
        if (IRI.isAssignableFrom(iri.getClass())) {
            def expectedTypeAnnotation = "@Type(\"${iri}\")"
            assert classFile.text.contains(expectedTypeAnnotation): "Class ${className} is missing the @Type annotation: ${expectedTypeAnnotation}"
        }else{
            println "Ignoring bnode class: ${iri} - ${className}"
        }

    }

    println "Ontology ${ontologyName} generated sources successfully verified."
}

println "All ontologies successfully verified."