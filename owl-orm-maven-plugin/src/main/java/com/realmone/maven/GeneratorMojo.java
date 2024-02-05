package com.realmone.maven;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.realmone.owl.orm.OrmException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.*;

@Mojo(name = "generate-orm", threadSafe = true)
public class GeneratorMojo extends AbstractMojo {

    private static final ModelFactory MODEL_FACTORY = new DynamicModelFactory();

    @Parameter(alias = "generates", required = true)
    private List<Ontology> generates;

    /**
     * List of reference ontologies.
     */
    @Parameter(alias = "references")
    private List<Ontology> references;

    /**
     * The location where the generated source will be stored.
     */
    @Parameter(property = "outputLocation", required = true,
            defaultValue = "${project.basedir}/target/generated-sources")
    private String outputLocation;

    private final String prolog;

    private final FileSystemManager fileSystemManager;

    public GeneratorMojo() throws MojoExecutionException {
        super();
        try {
            fileSystemManager = VFS.getManager();
        } catch (FileSystemException e) {
            throw new MojoExecutionException("Issue initializing VFS system to fetch ontologies!", e);
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("prolog.txt")) {
            if (is != null) {
                this.prolog = IOUtils.toString(is, Charset.defaultCharset());
            } else {
                throw new MojoExecutionException("Prolog file could not be found");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Issue loading prolog data for file headers", e);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureDirectoryExists(outputLocation);
        OwlOrmGenerator generator = OwlOrmGenerator.builder()
                .useModel(MODEL_FACTORY.createEmptyModel())
                .useGenerate(generates)
                .useReferences(references)
                .useFileSystemManager(fileSystemManager)
                .useOutputLocation(outputLocation)
                .useProlog(prolog)
                .useLog(getLog())
                .build();
        getLog().info(String.format("Loaded %d statements into memory in support of ORM generation",
                generator.getSize()));
        generator.run();
    }

    private static void ensureDirectoryExists(String outputLocation) {
        File f = new File(outputLocation);
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new OrmException("Couldn't generate output directory: " + outputLocation);
            }
        }
    }
}
