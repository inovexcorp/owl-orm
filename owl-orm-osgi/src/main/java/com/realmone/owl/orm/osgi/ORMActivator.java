package com.realmone.owl.orm.osgi;

import com.realmone.owl.orm.Thing;
import com.realmone.owl.orm.ThingFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class ORMActivator {
    private static final Logger LOG = LoggerFactory.getLogger(ORMActivator.class);
    private static final ValueFactory vf = new ValidatingValueFactory();

    List<Class<? extends Thing>> ormClasses = new ArrayList<>();

    @Reference
    protected OsgiThingFactory thingFactory;

    protected void start(BundleContext bundleContext) {
        LOG.debug("Starting ORM Activator for {}", bundleContext.getBundle().getSymbolicName());
        Dictionary<String, String> headers = bundleContext.getBundle().getHeaders();
        String ormClasses = headers.get("ORM-Classes");
        if (ormClasses != null) {
            LOG.debug("Found the following header value: {}", ormClasses);
            for (String className : ormClasses.split(",")) {
                try {
                    Class<?> clazz = bundleContext.getBundle().loadClass(className);
                    if (clazz.isInterface() && Thing.class.isAssignableFrom(clazz)) {
                        Class<? extends Thing> ormClass = (Class<? extends Thing>) clazz;
                        this.ormClasses.add(ormClass);
                        thingFactory.getTypeAnnotation(ormClass).ifPresent(type ->
                                thingFactory.addClass(vf.createIRI(type.value()), ormClass));
                    } else {
                        LOG.trace("Class {} is not an ORM class", className);
                    }
                } catch (ClassNotFoundException ex) {
                    LOG.trace("Could not load class {}", className);
                }
            }
        } else {
            LOG.debug("No ORM-Classes Manifest header found");
        }

        LOG.debug("Adding BundleClassLoader");
        thingFactory.addClassLoader(bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader());
    }

    protected void stop(BundleContext bundleContext) {
        LOG.debug("Stopping ORM Activator");
        ormClasses.forEach(ormClass ->
                thingFactory.getTypeAnnotation(ormClass).ifPresent(type ->
                        thingFactory.removeClass(vf.createIRI(type.value()))));
        ormClasses.clear();
        thingFactory.removeClassLoader(bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader());
    }
}
