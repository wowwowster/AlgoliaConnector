package com.sword.gsa.spis.scs.extracting.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.sword.gsa.spis.scs.extracting.detect.SCSDefaultDetector;
import com.sword.gsa.spis.scs.extracting.detect.SCSDefaultEncodingDetector;
import com.sword.gsa.spis.scs.extracting.language.translate.SCSDefaultTranslator;
import com.sword.gsa.spis.scs.extracting.parser.SCSParser;
import com.sword.gsa.spis.scs.extracting.parser.SCSAutoDetectParser;
import com.sword.gsa.spis.scs.extracting.parser.SCSCompositeParser;
import com.sword.gsa.spis.scs.extracting.parser.SCSDefaultParser;
import com.sword.gsa.spis.scs.extracting.parser.SCSParserDecorator;
import org.apache.tika.concurrent.ConfigurableThreadPoolExecutor;
import org.apache.tika.concurrent.SimpleThreadPoolExecutor;
import org.apache.tika.config.Initializable;
import org.apache.tika.config.InitializableProblemHandler;
import org.apache.tika.config.LoadErrorHandler;
import org.apache.tika.config.Param;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.CompositeEncodingDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaConfigException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.translate.Translator;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;
import org.apache.tika.parser.AbstractEncodingDetectorParser;
import org.apache.tika.utils.AnnotationUtils;
import org.apache.tika.utils.XMLReaderUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ExtractionConfig {

    private static MimeTypes getDefaultMimeTypes(ClassLoader loader) {
        return MimeTypes.getDefaultMimeTypes(loader);
    }


    protected static CompositeDetector getDefaultDetector(
            MimeTypes types, SCSServiceLoader loader) {
        return new SCSDefaultDetector(types, loader);
    }

    protected static CompositeEncodingDetector getDefaultEncodingDetector(
            SCSServiceLoader loader) {
        return new SCSDefaultEncodingDetector(loader);
    }


    private static SCSCompositeParser getDefaultParser(
            MimeTypes types, SCSServiceLoader loader, EncodingDetector encodingDetector) {
        return new SCSDefaultParser(types.getMediaTypeRegistry(), loader, encodingDetector);
    }

    private static Translator getDefaultTranslator(SCSServiceLoader loader) {
        return new SCSDefaultTranslator(loader);
    }

    private static ConfigurableThreadPoolExecutor getDefaultExecutorService() {
        return new SimpleThreadPoolExecutor();
    }

    //use this to look for unneeded instantiations of TikaConfig
    protected static AtomicInteger TIMES_INSTANTIATED = new AtomicInteger();

    private final SCSServiceLoader serviceLoader;
    private final SCSCompositeParser parser;
    private final CompositeDetector detector;
    private final Translator translator;

    private final MimeTypes mimeTypes;
    private final ExecutorService executorService;
    private final EncodingDetector encodingDetector;

    public ExtractionConfig(String file)
            throws TikaException, IOException, SAXException {
        this(Paths.get(file));
    }

    public ExtractionConfig(Path path)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(path.toFile()));
    }

    public ExtractionConfig(Path path, SCSServiceLoader loader)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(path.toFile()), loader);
    }

    public ExtractionConfig(File file)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(file));
    }

    public ExtractionConfig(File file, SCSServiceLoader loader)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(file), loader);
    }

    public ExtractionConfig(URL url)
            throws TikaException, IOException, SAXException {
        this(url, SCSServiceLoader.getContextClassLoader());
    }

    public ExtractionConfig(URL url, ClassLoader loader)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(url.toString()).getDocumentElement(), loader);
    }

    public ExtractionConfig(URL url, SCSServiceLoader loader)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(url.toString()).getDocumentElement(), loader);
    }

    public ExtractionConfig(InputStream stream)
            throws TikaException, IOException, SAXException {
        this(XMLReaderUtils.getDocumentBuilder().parse(stream));
    }

    public ExtractionConfig(Document document) throws TikaException, IOException {
        this(document.getDocumentElement());
    }

    public ExtractionConfig(Document document, SCSServiceLoader loader) throws TikaException, IOException {
        this(document.getDocumentElement(), loader);
    }

    public ExtractionConfig(Element element) throws TikaException, IOException {
        this(element, serviceLoaderFromDomElement(element, null));
    }

    public ExtractionConfig(Element element, ClassLoader loader)
            throws TikaException, IOException {
        this(element, serviceLoaderFromDomElement(element, loader));
    }

    private ExtractionConfig(Element element, SCSServiceLoader loader)
            throws TikaException, IOException {
        DetectorXmlLoader detectorLoader = new DetectorXmlLoader();
        TranslatorXmlLoader translatorLoader = new TranslatorXmlLoader();
        ExecutorServiceXmlLoader executorLoader = new ExecutorServiceXmlLoader();
        EncodingDetectorXmlLoader encodingDetectorXmlLoader = new EncodingDetectorXmlLoader();
        this.mimeTypes = typesFromDomElement(element);
        this.detector = detectorLoader.loadOverall(element, mimeTypes, loader);
        this.encodingDetector = encodingDetectorXmlLoader.loadOverall(element, mimeTypes, loader);

        ParserXmlLoader parserLoader = new ParserXmlLoader(encodingDetector);
        this.parser = parserLoader.loadOverall(element, mimeTypes, loader);
        this.translator = translatorLoader.loadOverall(element, mimeTypes, loader);
        this.executorService = executorLoader.loadOverall(element, mimeTypes, loader);
        this.serviceLoader = loader;
        TIMES_INSTANTIATED.incrementAndGet();
    }

    public ExtractionConfig(ClassLoader loader)
            throws MimeTypeException, IOException {
        this.serviceLoader = new SCSServiceLoader(loader);
        this.mimeTypes = getDefaultMimeTypes(loader);
        this.detector = getDefaultDetector(mimeTypes, serviceLoader);
        this.encodingDetector = getDefaultEncodingDetector(serviceLoader);
        this.parser = getDefaultParser(mimeTypes, serviceLoader, encodingDetector);
        this.translator = getDefaultTranslator(serviceLoader);
        this.executorService = getDefaultExecutorService();
        TIMES_INSTANTIATED.incrementAndGet();
    }

    public ExtractionConfig() throws TikaException, IOException {

        String config = System.getProperty("extracting.config");
        if (config == null) {
            config = System.getenv("TIKA_CONFIG");
        }

        if (config == null) {
            this.serviceLoader = new SCSServiceLoader();
            this.mimeTypes = getDefaultMimeTypes(SCSServiceLoader.getContextClassLoader());
            this.encodingDetector = getDefaultEncodingDetector(serviceLoader);
            this.parser = getDefaultParser(mimeTypes, serviceLoader, encodingDetector);
            this.detector = getDefaultDetector(mimeTypes, serviceLoader);
            this.translator = getDefaultTranslator(serviceLoader);
            this.executorService = getDefaultExecutorService();
        } else {
            SCSServiceLoader tmpServiceLoader = new SCSServiceLoader();
            try (InputStream stream = getConfigInputStream(config, tmpServiceLoader)) {
                Element element = XMLReaderUtils.getDocumentBuilder().parse(stream).getDocumentElement();
                serviceLoader = serviceLoaderFromDomElement(element, tmpServiceLoader.getLoader());
                DetectorXmlLoader detectorLoader = new DetectorXmlLoader();
                EncodingDetectorXmlLoader encodingDetectorLoader = new EncodingDetectorXmlLoader();
                TranslatorXmlLoader translatorLoader = new TranslatorXmlLoader();
                ExecutorServiceXmlLoader executorLoader = new ExecutorServiceXmlLoader();

                this.mimeTypes = typesFromDomElement(element);
                this.encodingDetector = encodingDetectorLoader.loadOverall(element, mimeTypes, serviceLoader);


                ParserXmlLoader parserLoader = new ParserXmlLoader(encodingDetector);
                this.parser = parserLoader.loadOverall(element, mimeTypes, serviceLoader);
                this.detector = detectorLoader.loadOverall(element, mimeTypes, serviceLoader);
                this.translator = translatorLoader.loadOverall(element, mimeTypes, serviceLoader);
                this.executorService = executorLoader.loadOverall(element, mimeTypes, serviceLoader);
            } catch (SAXException e) {
                throw new TikaException(
                        "Specified Tika configuration has syntax errors: "
                                + config, e);
            }
        }
        TIMES_INSTANTIATED.incrementAndGet();
    }

    private static InputStream getConfigInputStream(String config, SCSServiceLoader serviceLoader)
            throws TikaException, IOException {
        InputStream stream = null;
        try {
            stream = new URL(config).openStream();
        } catch (IOException ignore) {
        }
        if (stream == null) {
            stream = serviceLoader.getResourceAsStream(config);
        }
        if (stream == null) {
            Path file = Paths.get(config);
            if (Files.isRegularFile(file)) {
                stream = Files.newInputStream(file);
            }
        }
        if (stream == null) {
            throw new TikaException(
                    "Specified Tika configuration not found: " + config);
        }
        return stream;
    }

    private static String getText(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            StringBuilder builder = new StringBuilder();
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                builder.append(getText(list.item(i)));
            }
            return builder.toString();
        } else {
            return "";
        }
    }

    /**
     * @deprecated Use the {@link #getParser()} method instead
     */
    public SCSParser getParser(MediaType mimeType) {
        return parser.getParsers().get(mimeType);
    }

    /**
     * Returns the configured parser instance.
     *
     * @return configured parser
     */
    public SCSParser getParser() {
        return parser;
    }

    /**
     * Returns the configured detector instance.
     *
     * @return configured detector
     */
    public Detector getDetector() {
        return detector;
    }

    /**
     * Returns the configured encoding detector instance
     *
     * @return configured encoding detector
     */
    public EncodingDetector getEncodingDetector() {
        return encodingDetector;
    }

    /**
     * Returns the configured translator instance.
     *
     * @return configured translator
     */
    public Translator getTranslator() {
        return translator;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public MimeTypes getMimeRepository() {
        return mimeTypes;
    }

    public MediaTypeRegistry getMediaTypeRegistry() {
        return mimeTypes.getMediaTypeRegistry();
    }

    public SCSServiceLoader getServiceLoader() {
        return serviceLoader;
    }

    /**
     * Provides a default configuration (MyTikaConfig).  Currently creates a
     * new instance each time it's called; we may be able to have it
     * return a shared instance once it is completely immutable.
     *
     * @return default configuration
     */
    public static ExtractionConfig getDefaultConfig() {
        try {
            return new ExtractionConfig();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read default configuration", e);
        } catch (TikaException e) {
            throw new RuntimeException(
                    "Unable to access default configuration", e);
        }
    }

    private static Element getChild(Element element, String name) {
        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && name.equals(child.getNodeName())) {
                return (Element) child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private static List<Element> getTopLevelElementChildren(Element element,
                                                            String parentName, String childrenName) throws TikaException {
        Node parentNode = null;
        if (parentName != null) {
            // Should be only zero or one <parsers> / <detectors> etc tag
            NodeList nodes = element.getElementsByTagName(parentName);
            if (nodes.getLength() > 1) {
                throw new TikaException("Properties may not contain multiple " + parentName + " entries");
            } else if (nodes.getLength() == 1) {
                parentNode = nodes.item(0);
            }
        } else {
            // All children directly on the master element
            parentNode = element;
        }

        if (parentNode != null) {
            // Find only the direct child parser/detector objects
            NodeList nodes = parentNode.getChildNodes();
            List<Element> elements = new ArrayList<Element>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element) {
                    Element nodeE = (Element) node;
                    if (childrenName.equals(nodeE.getTagName())) {
                        elements.add(nodeE);
                    }
                }
            }
            return elements;
        } else {
            // No elements of this type
            return Collections.emptyList();
        }
    }

    private static MimeTypes typesFromDomElement(Element element)
            throws TikaException, IOException {
        Element mtr = getChild(element, "mimeTypeRepository");
        if (mtr != null && mtr.hasAttribute("resource")) {
            return MimeTypesFactory.create(mtr.getAttribute("resource"));
        } else {
            return getDefaultMimeTypes(null);
        }
    }

    private static Set<MediaType> mediaTypesListFromDomElement(
            Element node, String tag)
            throws TikaException, IOException {
        Set<MediaType> types = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node cNode = children.item(i);
            if (cNode instanceof Element) {
                Element cElement = (Element) cNode;
                if (tag.equals(cElement.getTagName())) {
                    String mime = getText(cElement);
                    MediaType type = MediaType.parse(mime);
                    if (type != null) {
                        if (types == null) types = new HashSet<>();
                        types.add(type);
                    } else {
                        throw new TikaException(
                                "Invalid media type name: " + mime);
                    }
                }
            }
        }
        if (types != null) return types;
        return Collections.emptySet();
    }

    private static SCSServiceLoader serviceLoaderFromDomElement(Element element, ClassLoader loader) throws TikaConfigException {
        Element serviceLoaderElement = getChild(element, "service-loader");
        SCSServiceLoader serviceLoader;

        if (serviceLoaderElement != null) {
            boolean dynamic = Boolean.parseBoolean(serviceLoaderElement.getAttribute("dynamic"));
            LoadErrorHandler loadErrorHandler = LoadErrorHandler.IGNORE;
            String loadErrorHandleConfig = serviceLoaderElement.getAttribute("loadErrorHandler");
            if (LoadErrorHandler.WARN.toString().equalsIgnoreCase(loadErrorHandleConfig)) {
                loadErrorHandler = LoadErrorHandler.WARN;
            } else if (LoadErrorHandler.THROW.toString().equalsIgnoreCase(loadErrorHandleConfig)) {
                loadErrorHandler = LoadErrorHandler.THROW;
            }
            InitializableProblemHandler initializableProblemHandler = getInitializableProblemHandler(serviceLoaderElement.getAttribute("initializableProblemHandler"));

            if (loader == null) {
                loader = SCSServiceLoader.getContextClassLoader();
            }
            serviceLoader = new SCSServiceLoader(loader, loadErrorHandler, initializableProblemHandler, dynamic);
        } else if (loader != null) {
            serviceLoader = new SCSServiceLoader(loader);
        } else {
            serviceLoader = new SCSServiceLoader();
        }
        return serviceLoader;
    }

    private static InitializableProblemHandler getInitializableProblemHandler(String initializableProblemHandler)
            throws TikaConfigException {
        if (initializableProblemHandler == null || initializableProblemHandler.length() == 0) {
            return InitializableProblemHandler.DEFAULT;
        }
        if (InitializableProblemHandler.IGNORE.toString().equalsIgnoreCase(initializableProblemHandler)) {
            return InitializableProblemHandler.IGNORE;
        } else if (InitializableProblemHandler.INFO.toString().equalsIgnoreCase(initializableProblemHandler)) {
            return InitializableProblemHandler.INFO;
        } else if (InitializableProblemHandler.WARN.toString().equalsIgnoreCase(initializableProblemHandler)) {
            return InitializableProblemHandler.WARN;
        } else if (InitializableProblemHandler.THROW.toString().equalsIgnoreCase(initializableProblemHandler)) {
            return InitializableProblemHandler.THROW;
        }
        throw new TikaConfigException(
                String.format(Locale.US, "Couldn't parse non-null '%s'. Must be one of 'ignore', 'info', 'warn' or 'throw'",
                        initializableProblemHandler));
    }


    private static abstract class XmlLoader<CT, T> {
        protected static final String PARAMS_TAG_NAME = "params";

        abstract boolean supportsComposite();

        abstract String getParentTagName(); // eg parsers

        abstract String getLoaderTagName(); // eg parser

        abstract Class<? extends T> getLoaderClass(); // Generics workaround

        abstract boolean isComposite(T loaded);

        abstract boolean isComposite(Class<? extends T> loadedClass);

        abstract T preLoadOne(Class<? extends T> loadedClass, String classname,
                              MimeTypes mimeTypes) throws TikaException;

        abstract CT createDefault(MimeTypes mimeTypes, SCSServiceLoader loader);

        abstract CT createComposite(List<T> loaded, MimeTypes mimeTypes, SCSServiceLoader loader);

        abstract T createComposite(Class<? extends T> compositeClass,
                                   List<T> children, Set<Class<? extends T>> excludeChildren,
                                   MimeTypes mimeTypes, SCSServiceLoader loader)
                throws InvocationTargetException, IllegalAccessException, InstantiationException;

        abstract T decorate(T created, Element element)
                throws IOException, TikaException; // eg explicit mime types

        @SuppressWarnings("unchecked")
        CT loadOverall(Element element, MimeTypes mimeTypes,
                       SCSServiceLoader loader) throws TikaException, IOException {
            List<T> loaded = new ArrayList<T>();

            // Find the children of the parent tag, if any
            for (Element le : getTopLevelElementChildren(element, getParentTagName(), getLoaderTagName())) {
                T loadedChild = loadOne(le, mimeTypes, loader);
                if (loadedChild != null) loaded.add(loadedChild);
            }

            // Build the classes, and wrap as needed
            if (loaded.isEmpty()) {
                // Nothing defined, create a Default
                return createDefault(mimeTypes, loader);
            } else if (loaded.size() == 1) {
                T single = loaded.get(0);
                if (isComposite(single)) {
                    // Single Composite defined, use that
                    return (CT) single;
                }
            } else if (!supportsComposite()) {
                // No composite support, just return the first one
                return (CT) loaded.get(0);
            }
            // Wrap the defined parsers/detectors up in a Composite
            return createComposite(loaded, mimeTypes, loader);
        }

        T loadOne(Element element, MimeTypes mimeTypes, SCSServiceLoader loader)
                throws TikaException, IOException {
            String name = element.getAttribute("class");

            String initProbHandler = element.getAttribute("initializableProblemHandler");
            InitializableProblemHandler initializableProblemHandler;
            if (initProbHandler == null || initProbHandler.length() == 0) {
                initializableProblemHandler = loader.getInitializableProblemHandler();
            } else {
                initializableProblemHandler =
                        getInitializableProblemHandler(initProbHandler);
            }

            T loaded = null;

            try {
                Class<? extends T> loadedClass = loader.getServiceClass(getLoaderClass(), name);

                // Do pre-load checks and short-circuits
                //TODO : allow duplicate instances with different configurations
                loaded = preLoadOne(loadedClass, name, mimeTypes);
                if (loaded != null) return loaded;

                // Is this a composite or decorated class? If so, support recursion
                if (isComposite(loadedClass)) {
                    // Get the child objects for it
                    List<T> children = new ArrayList<T>();
                    NodeList childNodes = element.getElementsByTagName(getLoaderTagName());
                    if (childNodes.getLength() > 0) {
                        for (int i = 0; i < childNodes.getLength(); i++) {
                            T loadedChild = loadOne((Element) childNodes.item(i),
                                    mimeTypes, loader);
                            if (loadedChild != null) children.add(loadedChild);
                        }
                    }

                    // Get the list of children to exclude
                    Set<Class<? extends T>> excludeChildren = new HashSet<Class<? extends T>>();
                    NodeList excludeChildNodes = element.getElementsByTagName(getLoaderTagName() + "-exclude");
                    if (excludeChildNodes.getLength() > 0) {
                        for (int i = 0; i < excludeChildNodes.getLength(); i++) {
                            Element excl = (Element) excludeChildNodes.item(i);
                            String exclName = excl.getAttribute("class");
                            excludeChildren.add(loader.getServiceClass(getLoaderClass(), exclName));
                        }
                    }

                    // Create the Composite
                    loaded = createComposite(loadedClass, children, excludeChildren, mimeTypes, loader);

                    // Default constructor fallback
                    if (loaded == null) {
                        loaded = newInstance(loadedClass);
                    }
                } else {
                    // Regular class, create as-is
                    loaded = newInstance(loadedClass);
                    // TODO Support arguments, needed for Translators etc
                    // See the thread "Configuring parsers and translators" for details
                }

                Map<String, Param> params = getParams(element);
                //Assigning the params to bean fields/setters
                AnnotationUtils.assignFieldParams(loaded, params);
                if (loaded instanceof Initializable) {
                    ((Initializable) loaded).initialize(params);
                    ((Initializable) loaded).checkInitialization(initializableProblemHandler);
                }
                // Have any decoration performed, eg explicit mimetypes
                loaded = decorate(loaded, element);
                // All done with setup
                return loaded;
            } catch (ClassNotFoundException e) {
                if (loader.getLoadErrorHandler() == LoadErrorHandler.THROW) {
                    // Use a different exception signature here
                    throw new TikaException(
                            "Unable to find a " + getLoaderTagName() + " class: " + name, e);
                }
                // Report the problem
                loader.getLoadErrorHandler().handleLoadError(name, e);
                return null;
            } catch (IllegalAccessException e) {
                throw new TikaException(
                        "Unable to access a " + getLoaderTagName() + " class: " + name, e);
            } catch (InvocationTargetException e) {
                throw new TikaException(
                        "Unable to create a " + getLoaderTagName() + " class: " + name, e);
            } catch (InstantiationException e) {
                throw new TikaException(
                        "Unable to instantiate a " + getLoaderTagName() + " class: " + name, e);
            } catch (NoSuchMethodException e) {
                throw new TikaException(
                        "Unable to find the right constructor for " + getLoaderTagName() + " class: " + name, e);
            }
        }


        T newInstance(Class<? extends T> loadedClass) throws
                IllegalAccessException, InstantiationException,
                NoSuchMethodException, InvocationTargetException {
            return loadedClass.newInstance();
        }

        /**
         * Gets parameters from a given
         *
         * @param el xml node which has {@link #PARAMS_TAG_NAME} child
         * @return Map of key values read from xml
         */
        Map<String, Param> getParams(Element el) {
            Map<String, Param> params = new HashMap<>();
            for (Node child = el.getFirstChild(); child != null;
                 child = child.getNextSibling()) {
                if (PARAMS_TAG_NAME.equals(child.getNodeName())) { //found the node
                    if (child.hasChildNodes()) {//it has children
                        NodeList childNodes = child.getChildNodes();
                        for (int i = 0; i < childNodes.getLength(); i++) {
                            Node item = childNodes.item(i);
                            if (item.getNodeType() == Node.ELEMENT_NODE) {
                                Param<?> param = Param.load(item);
                                params.put(param.getName(), param);
                            }
                        }
                    }
                    break; //only the first one is used
                }
            }
            return params;
        }

    }

    private static class ParserXmlLoader extends XmlLoader<SCSCompositeParser, SCSParser> {

        private final EncodingDetector encodingDetector;

        boolean supportsComposite() {
            return true;
        }

        String getParentTagName() {
            return "parsers";
        }

        String getLoaderTagName() {
            return "parser";
        }

        private ParserXmlLoader(EncodingDetector encodingDetector) {
            this.encodingDetector = encodingDetector;
        }

        @Override
        Class<? extends SCSParser> getLoaderClass() {
            return SCSParser.class;
        }

        @Override
        SCSParser preLoadOne(Class<? extends SCSParser> loadedClass, String classname,
                             MimeTypes mimeTypes) throws TikaException {
            // Check for classes which can't be set in config
            if (SCSAutoDetectParser.class.isAssignableFrom(loadedClass)) {
                // https://issues.apache.org/jira/browse/TIKA-866
                throw new TikaException(
                        "MyAutoDetectParser not supported in a <parser>"
                                + " configuration element: " + classname);
            }
            // Continue with normal loading
            return null;
        }

        @Override
        boolean isComposite(SCSParser loaded) {
            return loaded instanceof SCSCompositeParser;
        }

        @Override
        boolean isComposite(Class<? extends SCSParser> loadedClass) {
            if (SCSCompositeParser.class.isAssignableFrom(loadedClass) ||
                    SCSParserDecorator.class.isAssignableFrom(loadedClass)) {
                return true;
            }
            return false;
        }

        @Override
        SCSCompositeParser createDefault(MimeTypes mimeTypes, SCSServiceLoader loader) {
            return getDefaultParser(mimeTypes, loader, encodingDetector);
        }

        @Override
        SCSCompositeParser createComposite(List<SCSParser> parsers, MimeTypes mimeTypes, SCSServiceLoader loader) {
            MediaTypeRegistry registry = mimeTypes.getMediaTypeRegistry();
            return new SCSCompositeParser(registry, parsers);
        }

        @Override
        SCSParser createComposite(Class<? extends SCSParser> parserClass,
                                  List<SCSParser> childParsers, Set<Class<? extends SCSParser>> excludeParsers,
                                  MimeTypes mimeTypes, SCSServiceLoader loader)
                throws InvocationTargetException, IllegalAccessException, InstantiationException {
            SCSParser parser = null;
            Constructor<? extends SCSParser> c = null;
            MediaTypeRegistry registry = mimeTypes.getMediaTypeRegistry();

            // Try the possible default and composite parser constructors
            if (parser == null) {
                try {
                    c = parserClass.getConstructor(MediaTypeRegistry.class,
                            SCSServiceLoader.class, Collection.class, EncodingDetector.class);
                    parser = c.newInstance(registry, loader, excludeParsers, encodingDetector);
                } catch (NoSuchMethodException me) {
                }
            }
            if (parser == null) {
                try {
                    c = parserClass.getConstructor(MediaTypeRegistry.class, SCSServiceLoader.class, Collection.class);
                    parser = c.newInstance(registry, loader, excludeParsers);
                } catch (NoSuchMethodException me) {
                }
            }
            if (parser == null) {
                try {
                    c = parserClass.getConstructor(MediaTypeRegistry.class, List.class, Collection.class);
                    parser = c.newInstance(registry, childParsers, excludeParsers);
                } catch (NoSuchMethodException me) {
                }
            }
            if (parser == null) {
                try {
                    c = parserClass.getConstructor(MediaTypeRegistry.class, List.class);
                    parser = c.newInstance(registry, childParsers);
                } catch (NoSuchMethodException me) {
                }
            }

            // Create as a Parser Decorator
            if (parser == null && SCSParserDecorator.class.isAssignableFrom(parserClass)) {
                try {
                    SCSCompositeParser cp = null;
                    if (childParsers.size() == 1 && excludeParsers.size() == 0 &&
                            childParsers.get(0) instanceof SCSCompositeParser) {
                        cp = (SCSCompositeParser) childParsers.get(0);
                    } else {
                        cp = new SCSCompositeParser(registry, childParsers, excludeParsers);
                    }
                    c = parserClass.getConstructor(SCSParser.class);
                    parser = c.newInstance(cp);
                } catch (NoSuchMethodException me) {
                }
            }
            return parser;
        }

        @Override
        SCSParser newInstance(Class<? extends SCSParser> loadedClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            if (AbstractEncodingDetectorParser.class.isAssignableFrom(loadedClass)) {
                Constructor ctor = loadedClass.getConstructor(EncodingDetector.class);
                return (SCSParser) ctor.newInstance(encodingDetector);
            } else {
                return loadedClass.newInstance();
            }
        }

        @Override
        SCSParser decorate(SCSParser created, Element element) throws IOException, TikaException {
            SCSParser parser = created;

            // Is there an explicit list of mime types for this to handle?
            Set<MediaType> parserTypes = mediaTypesListFromDomElement(element, "mime");
            if (!parserTypes.isEmpty()) {
                parser = SCSParserDecorator.withTypes(parser, parserTypes);
            }
            // Is there an explicit list of mime types this shouldn't handle?
            Set<MediaType> parserExclTypes = mediaTypesListFromDomElement(element, "mime-exclude");
            if (!parserExclTypes.isEmpty()) {
                parser = SCSParserDecorator.withoutTypes(parser, parserExclTypes);
            }

            // All done with decoration
            return parser;
        }

    }

    private static class DetectorXmlLoader extends XmlLoader<CompositeDetector, Detector> {
        boolean supportsComposite() {
            return true;
        }

        String getParentTagName() {
            return "detectors";
        }

        String getLoaderTagName() {
            return "detector";
        }

        @Override
        Class<? extends Detector> getLoaderClass() {
            return Detector.class;
        }

        @Override
        Detector preLoadOne(Class<? extends Detector> loadedClass, String classname,
                            MimeTypes mimeTypes) throws TikaException {
            // If they asked for the mime types as a detector, give
            //  them the one we've already created. TIKA-1708
            if (MimeTypes.class.equals(loadedClass)) {
                return mimeTypes;
            }
            // Continue with normal loading
            return null;
        }

        @Override
        boolean isComposite(Detector loaded) {
            return loaded instanceof CompositeDetector;
        }

        @Override
        boolean isComposite(Class<? extends Detector> loadedClass) {
            return CompositeDetector.class.isAssignableFrom(loadedClass);
        }

        @Override
        CompositeDetector createDefault(MimeTypes mimeTypes, SCSServiceLoader loader) {
            return getDefaultDetector(mimeTypes, loader);
        }

        @Override
        CompositeDetector createComposite(List<Detector> detectors, MimeTypes mimeTypes, SCSServiceLoader loader) {
            MediaTypeRegistry registry = mimeTypes.getMediaTypeRegistry();
            return new CompositeDetector(registry, detectors);
        }

        @Override
        Detector createComposite(Class<? extends Detector> detectorClass,
                                 List<Detector> childDetectors,
                                 Set<Class<? extends Detector>> excludeDetectors,
                                 MimeTypes mimeTypes, SCSServiceLoader loader)
                throws InvocationTargetException, IllegalAccessException,
                InstantiationException {
            Detector detector = null;
            Constructor<? extends Detector> c;
            MediaTypeRegistry registry = mimeTypes.getMediaTypeRegistry();

            // Try the possible default and composite detector constructors
            if (detector == null) {
                try {
                    c = detectorClass.getConstructor(MimeTypes.class, SCSServiceLoader.class, Collection.class);
                    detector = c.newInstance(mimeTypes, loader, excludeDetectors);
                } catch (NoSuchMethodException me) {
                }
            }
            if (detector == null) {
                try {
                    c = detectorClass.getConstructor(MediaTypeRegistry.class, List.class, Collection.class);
                    detector = c.newInstance(registry, childDetectors, excludeDetectors);
                } catch (NoSuchMethodException me) {
                }
            }
            if (detector == null) {
                try {
                    c = detectorClass.getConstructor(MediaTypeRegistry.class, List.class);
                    detector = c.newInstance(registry, childDetectors);
                } catch (NoSuchMethodException me) {
                }
            }
            if (detector == null) {
                try {
                    c = detectorClass.getConstructor(List.class);
                    detector = c.newInstance(childDetectors);
                } catch (NoSuchMethodException me) {
                }
            }

            return detector;
        }

        @Override
        Detector decorate(Detector created, Element element) {
            return created; // No decoration of Detectors
        }
    }

    private static class TranslatorXmlLoader extends XmlLoader<Translator, Translator> {
        boolean supportsComposite() {
            return false;
        }

        String getParentTagName() {
            return null;
        }

        String getLoaderTagName() {
            return "translator";
        }

        @Override
        Class<? extends Translator> getLoaderClass() {
            return Translator.class;
        }

        @Override
        Translator preLoadOne(Class<? extends Translator> loadedClass, String classname,
                              MimeTypes mimeTypes) throws TikaException {
            // Continue with normal loading
            return null;
        }

        @Override
        boolean isComposite(Translator loaded) {
            return false;
        }

        @Override
        boolean isComposite(Class<? extends Translator> loadedClass) {
            return false;
        }

        @Override
        Translator createDefault(MimeTypes mimeTypes, SCSServiceLoader loader) {
            return getDefaultTranslator(loader);
        }

        @Override
        Translator createComposite(List<Translator> loaded,
                                   MimeTypes mimeTypes, SCSServiceLoader loader) {
            return loaded.get(0);
        }

        @Override
        Translator createComposite(Class<? extends Translator> compositeClass,
                                   List<Translator> children,
                                   Set<Class<? extends Translator>> excludeChildren,
                                   MimeTypes mimeTypes, SCSServiceLoader loader)
                throws InvocationTargetException, IllegalAccessException,
                InstantiationException {
            throw new InstantiationException("Only one translator supported");
        }

        @Override
        Translator decorate(Translator created, Element element) {
            return created; // No decoration of Translators
        }
    }

    private static class ExecutorServiceXmlLoader extends XmlLoader<ConfigurableThreadPoolExecutor, ConfigurableThreadPoolExecutor> {
        @Override
        ConfigurableThreadPoolExecutor createComposite(
                Class<? extends ConfigurableThreadPoolExecutor> compositeClass,
                List<ConfigurableThreadPoolExecutor> children,
                Set<Class<? extends ConfigurableThreadPoolExecutor>> excludeChildren,
                MimeTypes mimeTypes, SCSServiceLoader loader)
                throws InvocationTargetException, IllegalAccessException,
                InstantiationException {
            throw new InstantiationException("Only one executor service supported");
        }

        @Override
        ConfigurableThreadPoolExecutor createComposite(List<ConfigurableThreadPoolExecutor> loaded,
                                                       MimeTypes mimeTypes, SCSServiceLoader loader) {
            return loaded.get(0);
        }

        @Override
        ConfigurableThreadPoolExecutor createDefault(MimeTypes mimeTypes, SCSServiceLoader loader) {
            return getDefaultExecutorService();
        }

        @Override
        ConfigurableThreadPoolExecutor decorate(ConfigurableThreadPoolExecutor created, Element element)
                throws IOException, TikaException {

            Element maxThreadElement = getChild(element, "max-threads");
            if (maxThreadElement != null) {
                created.setMaximumPoolSize(Integer.parseInt(getText(maxThreadElement)));
            }

            Element coreThreadElement = getChild(element, "core-threads");
            if (coreThreadElement != null) {
                created.setCorePoolSize(Integer.parseInt(getText(coreThreadElement)));
            }
            return created;
        }

        @Override
        Class<? extends ConfigurableThreadPoolExecutor> getLoaderClass() {
            return ConfigurableThreadPoolExecutor.class;
        }

        @Override
        ConfigurableThreadPoolExecutor loadOne(Element element, MimeTypes mimeTypes,
                                               SCSServiceLoader loader) throws TikaException, IOException {
            return super.loadOne(element, mimeTypes, loader);
        }

        @Override
        boolean supportsComposite() {
            return false;
        }

        @Override
        String getParentTagName() {
            return null;
        }

        @Override
        String getLoaderTagName() {
            return "executor-service";
        }

        @Override
        boolean isComposite(ConfigurableThreadPoolExecutor loaded) {
            return false;
        }

        @Override
        boolean isComposite(Class<? extends ConfigurableThreadPoolExecutor> loadedClass) {
            return false;
        }

        @Override
        ConfigurableThreadPoolExecutor preLoadOne(
                Class<? extends ConfigurableThreadPoolExecutor> loadedClass, String classname,
                MimeTypes mimeTypes) throws TikaException {
            return null;
        }
    }

    private static class EncodingDetectorXmlLoader extends
            XmlLoader<EncodingDetector, EncodingDetector> {

        boolean supportsComposite() {
            return true;
        }

        String getParentTagName() {
            return "encodingDetectors";
        }

        String getLoaderTagName() {
            return "encodingDetector";
        }

        @Override
        Class<? extends EncodingDetector> getLoaderClass() {
            return EncodingDetector.class;
        }


        @Override
        boolean isComposite(EncodingDetector loaded) {
            return loaded instanceof CompositeEncodingDetector;
        }

        @Override
        boolean isComposite(Class<? extends EncodingDetector> loadedClass) {
            return CompositeEncodingDetector.class.isAssignableFrom(loadedClass);
        }

        @Override
        EncodingDetector preLoadOne(Class<? extends EncodingDetector> loadedClass,
                                    String classname, MimeTypes mimeTypes) throws TikaException {
            // Check for classes which can't be set in config
            // Continue with normal loading
            return null;
        }

        @Override
        EncodingDetector createDefault(MimeTypes mimeTypes, SCSServiceLoader loader) {
            return getDefaultEncodingDetector(loader);
        }

        @Override
        CompositeEncodingDetector createComposite(List<EncodingDetector> encodingDetectors, MimeTypes mimeTypes, SCSServiceLoader loader) {
            return new CompositeEncodingDetector(encodingDetectors);
        }

        @Override
        EncodingDetector createComposite(Class<? extends EncodingDetector> encodingDetectorClass,
                                         List<EncodingDetector> childEncodingDetectors,
                                         Set<Class<? extends EncodingDetector>> excludeDetectors,
                                         MimeTypes mimeTypes, SCSServiceLoader loader)
                throws InvocationTargetException, IllegalAccessException,
                InstantiationException {
            EncodingDetector encodingDetector = null;
            Constructor<? extends EncodingDetector> c;

            // Try the possible default and composite detector constructors
            if (encodingDetector == null) {
                try {
                    c = encodingDetectorClass.getConstructor(SCSServiceLoader.class, Collection.class);
                    encodingDetector = c.newInstance(loader, excludeDetectors);
                } catch (NoSuchMethodException me) {
                    me.printStackTrace();
                }
            }
            if (encodingDetector == null) {
                try {
                    c = encodingDetectorClass.getConstructor(List.class);
                    encodingDetector = c.newInstance(childEncodingDetectors);
                } catch (NoSuchMethodException me) {
                    me.printStackTrace();
                }
            }

            return encodingDetector;
        }

        @Override
        EncodingDetector decorate(EncodingDetector created, Element element) {
            return created; // No decoration of EncodingDetectors
        }
    }


}
