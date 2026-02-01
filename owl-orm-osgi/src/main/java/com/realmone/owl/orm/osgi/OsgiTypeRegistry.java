/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.osgi;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.TypeMetadata;
import com.realmone.owl.orm.TypeRegistry;
import com.realmone.owl.orm.basic.SimpleTypeRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * An OSGi service component that implements {@link TypeRegistry} and tracks bundles for automatic
 * type registration and unregistration.
 * <p>
 * This component uses the OSGi extender pattern to automatically discover and register
 * {@link TypeMetadata} instances from bundles as they start. When a bundle is started,
 * this registry scans it for TypeMetadata services using the Java ServiceLoader mechanism
 * (META-INF/services/com.realmone.owl.orm.TypeMetadata).
 * <p>
 * When a bundle is stopped, all types that were registered from that bundle are automatically
 * unregistered, ensuring clean lifecycle management.
 * <p>
 * This approach solves the race condition problem inherent in OSGi dynamic service references
 * because:
 * <ul>
 *   <li>Type metadata is registered during bundle STARTING phase, before services activate</li>
 *   <li>The registry is marked as {@code immediate = true}, ensuring it starts first</li>
 *   <li>TypeMetadata classes have no service dependencies, so they can be loaded immediately</li>
 * </ul>
 */
@Component(immediate = true, service = TypeRegistry.class)
public class OsgiTypeRegistry implements TypeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiTypeRegistry.class);

    /**
     * The delegate registry that stores the actual type metadata.
     */
    private final SimpleTypeRegistry delegate = new SimpleTypeRegistry();

    /**
     * Bundle tracker for automatic type discovery.
     */
    private BundleTracker<List<TypeMetadata<?>>> bundleTracker;

    /**
     * Activates the component and starts tracking bundles.
     *
     * @param context The bundle context
     */
    @Activate
    void activate(BundleContext context) {
        LOG.info("OsgiTypeRegistry activating - starting bundle tracker");

        // Track ACTIVE bundles for type metadata
        bundleTracker = new BundleTracker<>(context, Bundle.ACTIVE, new TypeMetadataTrackerCustomizer());
        bundleTracker.open();

        LOG.info("OsgiTypeRegistry activated - tracking {} bundles", bundleTracker.getTracked().size());
    }

    /**
     * Deactivates the component and stops tracking bundles.
     */
    @Deactivate
    void deactivate() {
        LOG.info("OsgiTypeRegistry deactivating");
        if (bundleTracker != null) {
            bundleTracker.close();
            bundleTracker = null;
        }
        delegate.clear();
        LOG.info("OsgiTypeRegistry deactivated");
    }

    // ========================================================================
    // TypeRegistry delegation methods
    // ========================================================================

    @Override
    public <T extends Thing> Optional<TypeMetadata<T>> getMetadata(Class<T> type) {
        return delegate.getMetadata(type);
    }

    @Override
    public Optional<TypeMetadata<? extends Thing>> getMetadata(org.eclipse.rdf4j.model.IRI typeIRI) {
        return delegate.getMetadata(typeIRI);
    }

    @Override
    public Optional<TypeMetadata<? extends Thing>> getMetadata(String typeIRI) {
        return delegate.getMetadata(typeIRI);
    }

    @Override
    public <T extends Thing> List<TypeMetadata<? extends T>> getMetadataForType(Class<T> type) {
        return delegate.getMetadataForType(type);
    }

    @Override
    public List<TypeMetadata<? extends Thing>> getMetadataForType(org.eclipse.rdf4j.model.IRI typeIRI) {
        return delegate.getMetadataForType(typeIRI);
    }

    @Override
    public <T extends Thing> List<TypeMetadata<? extends T>> getSortedMetadataForType(Class<T> type) {
        return delegate.getSortedMetadataForType(type);
    }

    @Override
    public List<TypeMetadata<? extends Thing>> getSortedMetadataForType(org.eclipse.rdf4j.model.IRI typeIRI) {
        return delegate.getSortedMetadataForType(typeIRI);
    }

    @Override
    public <T extends Thing> void register(TypeMetadata<T> metadata) {
        delegate.register(metadata);
    }

    @Override
    public <T extends Thing> void register(TypeMetadata<T> metadata, String source) {
        delegate.register(metadata, source);
    }

    @Override
    public <T extends Thing> boolean unregister(Class<T> type) {
        return delegate.unregister(type);
    }

    @Override
    public void unregisterBySource(String source) {
        delegate.unregisterBySource(source);
    }

    @Override
    public Set<org.eclipse.rdf4j.model.IRI> getRegisteredTypeIRIs() {
        return delegate.getRegisteredTypeIRIs();
    }

    @Override
    public Set<Class<? extends Thing>> getRegisteredTypes() {
        return delegate.getRegisteredTypes();
    }

    @Override
    public boolean isRegistered(Class<? extends Thing> type) {
        return delegate.isRegistered(type);
    }

    @Override
    public boolean isRegistered(org.eclipse.rdf4j.model.IRI typeIRI) {
        return delegate.isRegistered(typeIRI);
    }

    // ========================================================================
    // Bundle tracker customizer
    // ========================================================================

    /**
     * Customizer that scans bundles for TypeMetadata services and registers them.
     */
    private class TypeMetadataTrackerCustomizer implements BundleTrackerCustomizer<List<TypeMetadata<?>>> {

        @Override
        public List<TypeMetadata<?>> addingBundle(Bundle bundle, BundleEvent event) {
            List<TypeMetadata<?>> registered = new ArrayList<>();
            String source = bundle.getSymbolicName();

            try {
                // Use ServiceLoader to find TypeMetadata implementations in the bundle
                ServiceLoader<TypeMetadata> loader = ServiceLoader.load(
                        TypeMetadata.class,
                        new BundleClassLoader(bundle)
                );

                StreamSupport.stream(loader.spliterator(), false)
                        .forEach(metadata -> {
                            LOG.debug("Registering type {} from bundle {}",
                                    metadata.getType().getName(), source);
                            delegate.register(metadata, source);
                            registered.add(metadata);
                        });

                if (!registered.isEmpty()) {
                    LOG.info("Registered {} types from bundle {}", registered.size(), source);
                }
            } catch (Exception e) {
                LOG.debug("No TypeMetadata found in bundle {} (this is normal for non-ORM bundles): {}",
                        source, e.getMessage());
            }

            return registered.isEmpty() ? null : registered;
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event, List<TypeMetadata<?>> registered) {
            // No action needed on modification
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event, List<TypeMetadata<?>> registered) {
            if (registered != null && !registered.isEmpty()) {
                String source = bundle.getSymbolicName();
                LOG.info("Unregistering {} types from bundle {}", registered.size(), source);
                delegate.unregisterBySource(source);
            }
        }
    }

    /**
     * A ClassLoader that delegates to an OSGi Bundle for class loading.
     * This enables ServiceLoader to find services declared in a bundle's META-INF/services.
     */
    private static class BundleClassLoader extends ClassLoader {
        private final Bundle bundle;

        BundleClassLoader(Bundle bundle) {
            super(null); // No parent classloader
            this.bundle = bundle;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return bundle.loadClass(name);
        }

        @Override
        protected java.net.URL findResource(String name) {
            return bundle.getResource(name);
        }

        @Override
        protected java.util.Enumeration<java.net.URL> findResources(String name) throws java.io.IOException {
            return bundle.getResources(name);
        }
    }
}
