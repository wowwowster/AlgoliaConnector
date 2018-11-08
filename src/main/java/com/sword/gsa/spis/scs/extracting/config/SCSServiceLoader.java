package com.sword.gsa.spis.scs.extracting.config;

import org.apache.tika.config.Initializable;
import org.apache.tika.config.InitializableProblemHandler;
import org.apache.tika.config.LoadErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SCSServiceLoader {

    private static volatile ClassLoader contextClassLoader = null;
    private static final Logger logger = LoggerFactory.getLogger(SCSServiceLoader.class);
    public static final int CLASS_EXPECTED_ERROR = 3;

    private static class RankedService implements Comparable<RankedService> {
        private Object service;
        private int rank;

        public RankedService(Object service, int rank) {
            this.service = service;
            this.rank = rank;
        }

        public boolean isInstanceOf(Class<?> iface) {
            return iface.isAssignableFrom(service.getClass());
        }

        public int compareTo(RankedService that) {
            return that.rank - rank; // highest number first
        }

    }

    /**
     * The dynamic set of services available in an OSGi environment.
     * Managed by the {TikaActivator} class and used as an additional
     * source of service instances in the {@link #loadServiceProviders(Class)}
     * method.
     */
    private static final Map<Object, RankedService> services =
            new HashMap<Object, RankedService>();

    /**
     * Returns the context class loader of the current thread. If such
     * a class loader is not available, then the loader of this class or
     * finally the system class loader is returned.
     *
     * @see <a href="https://issues.apache.org/jira/browse/TIKA-441">TIKA-441</a>
     * @return context class loader, or <code>null</code> if no loader
     *         is available
     */
    static ClassLoader getContextClassLoader() {
        ClassLoader loader = contextClassLoader;
        if (loader == null) {
            loader = SCSServiceLoader.class.getClassLoader();
        }
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }

    public static void setContextClassLoader(ClassLoader loader) {
        contextClassLoader = loader;
    }

    static void addService(Object reference, Object service, int rank) {
        synchronized (services) {
            services.put(reference, new RankedService(service, rank));
        }
    }

    static Object removeService(Object reference) {
        synchronized (services) {
            return services.remove(reference);
        }
    }

    private final ClassLoader loader;

    private final LoadErrorHandler handler;
    private final InitializableProblemHandler initializableProblemHandler;

    private final boolean dynamic;

    public SCSServiceLoader(
            ClassLoader loader, LoadErrorHandler handler,
            InitializableProblemHandler initializableProblemHandler, boolean dynamic) {
        this.loader = loader;
        this.handler = handler;
        this.initializableProblemHandler = initializableProblemHandler;
        this.dynamic = dynamic;

    }
    public SCSServiceLoader(
            ClassLoader loader, LoadErrorHandler handler, boolean dynamic) {
        this(loader, handler, InitializableProblemHandler.WARN, dynamic);
    }

    public SCSServiceLoader(ClassLoader loader, LoadErrorHandler handler) {
        this(loader, handler, false);
    }

    public SCSServiceLoader(ClassLoader loader) {
        this(loader, Boolean.getBoolean("org.apache.extracting.service.error.warn")
                ? LoadErrorHandler.WARN:LoadErrorHandler.IGNORE);
    }

    public SCSServiceLoader() {
        this(getContextClassLoader(), Boolean.getBoolean("org.apache.extracting.service.error.warn")
                ? LoadErrorHandler.WARN:LoadErrorHandler.IGNORE, true);
    }

    /**
     * Returns if the service loader is static or dynamic
     *
     * @return dynamic or static loading
     * @since Apache Tika 1.10
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Returns the load error handler used by this loader.
     *
     * @return load error handler
     * @since Apache Tika 1.3
     */
    public LoadErrorHandler getLoadErrorHandler() {
        return handler;
    }

    /**
     * Returns the handler for problems with initializables
     *
     * @return handler for problems with initializables
     * @since Apache Tika 1.15.1
     */
    public InitializableProblemHandler getInitializableProblemHandler() {
        return initializableProblemHandler;
    }

    /**
     * Returns an input stream for reading the specified resource from the
     * configured class loader.
     *
     * @param name resource name
     * @return input stream, or <code>null</code> if the resource was not found
     * @see ClassLoader#getResourceAsStream(String)
     * @since Apache Tika 1.1
     */
    public InputStream getResourceAsStream(String name) {
        if (loader != null) {
            return loader.getResourceAsStream(name);
        } else {
            return null;
        }
    }

    /**
     *
     * @return ClassLoader used by this MyServiceLoader
     * @see #getContextClassLoader() for the context's ClassLoader
     * @since Apache Tika 1.15.1
     */
    public ClassLoader getLoader() {
        return loader;
    }

    /**
     * Loads and returns the named service class that's expected to implement
     * the given interface.
     *
     * Note that this class does not use the {@link LoadErrorHandler}, a
     *  {@link ClassNotFoundException} is always returned for unknown
     *  classes or classes of the wrong type
     *
     * @param iface service interface
     * @param name service class name
     * @return service class
     * @throws ClassNotFoundException if the service class can not be found
     *                                or does not implement the given interface
     * @see Class#forName(String, boolean, ClassLoader)
     * @since Apache Tika 1.1
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getServiceClass(Class<T> iface, String name)
            throws ClassNotFoundException {
        if (loader == null) {
            throw new ClassNotFoundException(
                    "Service class " + name + " is not available");
        }
        Class<?> klass = Class.forName(name, true, loader);
        if (klass.isInterface()) {
            throw new ClassNotFoundException(
                    "Service class " + name + " is an interface");
        } else if (!iface.isAssignableFrom(klass)) {
            throw new ClassNotFoundException(
                    "Service class " + name
                            + " does not implement " + iface.getName());
        } else {
            return (Class<? extends T>) klass;
        }
    }

    /**
     * Returns all the available service resources matching the
     *  given pattern, such as all instances of extracting-mimetypes.xml
     *  on the classpath, or all org.apache.extracting.parser.Parser
     *  service files.
     */
    public Enumeration<URL> findServiceResources(String filePattern) {
        try {
            Enumeration<URL> resources = loader.getResources(filePattern);
            return resources;
        } catch (IOException ignore) {
            // We couldn't get the list of service resource files
            List<URL> empty = Collections.emptyList();
            return Collections.enumeration( empty );
        }
    }

    /**
     * Returns all the available service providers of the given type.
     *
     * @param iface service provider interface
     * @return available service providers
     */
    public <T> List<T> loadServiceProviders(Class<T> iface) {
        List<T> providers = new ArrayList<T>();
        providers.addAll(loadDynamicServiceProviders(iface));
        providers.addAll(loadStaticServiceProviders(iface));
        return providers;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> loadDynamicServiceProviders(Class<T> iface) {
        if (dynamic) {
            synchronized (services) {
                List<RankedService> list =
                        new ArrayList<RankedService>(services.values());
                Collections.sort(list);

                List<T> providers = new ArrayList<T>(list.size());
                for (RankedService service : list) {
                    if (service.isInstanceOf(iface)) {
                        providers.add((T) service.service);
                    }
                }
                return providers;
            }
        } else {
            return new ArrayList<T>(0);
        }
    }

    protected <T> List<String> identifyStaticServiceProviders(Class<T> iface) {
        List<String> names = new ArrayList<String>();

        if (loader != null) {
            String serviceName = iface.getName();

            //if (serviceName.equalsIgnoreCase("com.sword.extracting.parser.pdf.PDFParser")) {
            if (serviceName.equalsIgnoreCase("com.sword.gsa.spis.scs.extracting.parser.SCSParser")) {
                names.add("com.sword.gsa.spis.scs.extracting.parser.pdf.SCSParserPDF");
                names.add("com.sword.gsa.spis.scs.extracting.parser.txt.SCSTXTParser");
                names.add("com.sword.gsa.spis.scs.extracting.parser.html.SCSHtmlParser");
                return names;
            }
            Enumeration<URL> resources =
                    findServiceResources("META-INF/services/" + serviceName);
            for (URL resource : Collections.list(resources)) {
                try {
                    collectServiceClassNames(resource, names);
                } catch (IOException e) {
                    handler.handleLoadError(serviceName, e);
                }
            }
        }

        return names;
    }

    /**
     * Returns the available static service providers of the given type.
     * The providers are loaded using the service provider mechanism using
     * the configured class loader (if any). The returned list is newly
     * allocated and may be freely modified by the caller.
     *
     * @since Apache Tika 1.2
     * @param iface service provider interface
     * @return static service providers
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> loadStaticServiceProviders(Class<T> iface) {
        List<T> providers = new ArrayList<T>();

        if (loader != null) {
            List<String> names = identifyStaticServiceProviders(iface);

            for (String name : names) {
                try {
                    Class<?> klass = loader.loadClass(name);
                    if (iface.isAssignableFrom(klass)) {
                        T instance = (T) klass.newInstance();
                        if (instance instanceof Initializable) {
                            ((Initializable)instance).checkInitialization(initializableProblemHandler);
                        }
                        providers.add(instance);
                    }
                /*} catch (final ClassNotFoundException cfe) {
                    logger.debug("Loading class failed: [" + cfe.getClass().getName() + "] " + cfe.getMessage());
                    logger.trace("StackTrace: ", cfe);
                    System.exit(CLASS_EXPECTED_ERROR);*/

                    /** TODO @author claurier
                     *  le catche suivant court circuite beaucoup de choses
                     *   voir s'il est indispensable ou modifiable
                     */
                } catch (Throwable t) {

                    handler.handleLoadError(name, t);
                }
            }
        }
        return providers;
    }

    private static final Pattern COMMENT = Pattern.compile("#.*");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private void collectServiceClassNames(URL resource, Collection<String> names)
            throws IOException {
        try (InputStream stream = resource.openStream()) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, UTF_8));
            String line = reader.readLine();
            while (line != null) {
                line = COMMENT.matcher(line).replaceFirst("");
                line = WHITESPACE.matcher(line).replaceAll("");
                if (line.length() > 0) {
                    names.add(line);
                }
                line = reader.readLine();
            }
        }
    }

}
