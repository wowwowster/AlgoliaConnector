package com.sword.gsa.spis.scs.extracting.parser;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.TaggedContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SCSCompositeParser extends SCSAbstractParser {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 2192845797749627824L;

    /**
     * Media type registry.
     */
    private MediaTypeRegistry registry;

    /**
     * List of component parsers.
     */
    private List<SCSParser> parsers;

    /**
     * The fallback parser, used when no better parser is available.
     */
    private SCSParser fallback = new SCSEmptyParser();

    public SCSCompositeParser(MediaTypeRegistry registry, List<SCSParser> parsers,
                              Collection<Class<? extends SCSParser>> excludeParsers) {
        if (excludeParsers == null || excludeParsers.isEmpty()) {
            this.parsers = parsers;
        } else {
            this.parsers = new ArrayList<SCSParser>();
            for (SCSParser p : parsers) {
                if (!isExcluded(excludeParsers, p.getClass())) {
                    this.parsers.add(p);
                }
            }
        }
        this.registry = registry;
    }

    public SCSCompositeParser(MediaTypeRegistry registry, List<SCSParser> parsers) {
        this(registry, parsers, null);
    }

    public SCSCompositeParser(MediaTypeRegistry registry, SCSParser... parsers) {
        this(registry, Arrays.asList(parsers));
    }

    public SCSCompositeParser() {
        this(new MediaTypeRegistry());
    }

    public Map<MediaType, SCSParser> getParsers(ParseContext context) {
        Map<MediaType, SCSParser> map = new HashMap<MediaType, SCSParser>();
        for (SCSParser parser : parsers) {
            for (MediaType type : parser.getSupportedTypes(context)) {
                map.put(registry.normalize(type), parser);
            }
        }
        return map;
    }

    private boolean isExcluded(Collection<Class<? extends SCSParser>> excludeParsers, Class<? extends SCSParser> p) {
        return excludeParsers.contains(p) || assignableFrom(excludeParsers, p);
    }

    private boolean assignableFrom(Collection<Class<? extends SCSParser>> excludeParsers, Class<? extends SCSParser> p) {
        for (Class<? extends SCSParser> e : excludeParsers) {
            if (e.isAssignableFrom(p)) return true;
        }
        return false;
    }

    /**
     * Utility method that goes through all the component parsers and finds
     * all media types for which more than one parser declares support. This
     * is useful in tracking down conflicting parser definitions.
     *
     * @param context parsing context
     * @return media types that are supported by at least two component parsers
     * @see <a href="https://issues.apache.org/jira/browse/TIKA-660">TIKA-660</a>
     * @since Apache Tika 0.10
     */
    public Map<MediaType, List<SCSParser>> findDuplicateParsers(
            ParseContext context) {
        Map<MediaType, SCSParser> types = new HashMap<MediaType, SCSParser>();
        Map<MediaType, List<SCSParser>> duplicates =
                new HashMap<MediaType, List<SCSParser>>();
        for (SCSParser parser : parsers) {
            for (MediaType type : parser.getSupportedTypes(context)) {
                MediaType canonicalType = registry.normalize(type);
                if (types.containsKey(canonicalType)) {
                    List<SCSParser> list = duplicates.get(canonicalType);
                    if (list == null) {
                        list = new ArrayList<SCSParser>();
                        list.add(types.get(canonicalType));
                        duplicates.put(canonicalType, list);
                    }
                    list.add(parser);
                } else {
                    types.put(canonicalType, parser);
                }
            }
        }
        return duplicates;
    }

    /**
     * Returns the media type registry used to infer type relationships.
     *
     * @return media type registry
     * @since Apache Tika 0.8
     */
    public MediaTypeRegistry getMediaTypeRegistry() {
        return registry;
    }

    /**
     * Sets the media type registry used to infer type relationships.
     *
     * @param registry media type registry
     * @since Apache Tika 0.8
     */
    public void setMediaTypeRegistry(MediaTypeRegistry registry) {
        this.registry = registry;
    }

    /**
     * Returns all parsers registered with the Composite Parser,
     * including ones which may not currently be active.
     * This won't include the Fallback Parser, if defined
     */
    public List<SCSParser> getAllComponentParsers() {
        return Collections.unmodifiableList(parsers);
    }

    /**
     * Returns the component parsers.
     *
     * @return component parsers, keyed by media type
     */
    public Map<MediaType, SCSParser> getParsers() {
        return getParsers(new ParseContext());
    }

    /**
     * Sets the component parsers.
     *
     * @param parsers component parsers, keyed by media type
     */
    public void setParsers(Map<MediaType, SCSParser> parsers) {
        this.parsers = new ArrayList<SCSParser>(parsers.size());
        for (Map.Entry<MediaType, SCSParser> entry : parsers.entrySet()) {
            this.parsers.add(SCSParserDecorator.withTypes(
                    entry.getValue(), Collections.singleton(entry.getKey())));
        }
    }

    /**
     * Returns the fallback parser.
     *
     * @return fallback parser
     */
    public SCSParser getFallback() {
        return fallback;
    }

    /**
     * Sets the fallback parser.
     *
     * @param fallback fallback parser
     */
    public void setFallback(SCSParser fallback) {
        this.fallback = fallback;
    }

    protected SCSParser getParser(Metadata metadata) {
        return getParser(metadata, new ParseContext());
    }

    protected SCSParser getParser(Metadata metadata, ParseContext context) {
        Map<MediaType, SCSParser> map = getParsers(context);
        MediaType type = MediaType.parse(metadata.get(Metadata.CONTENT_TYPE));
        if (type != null) {
            // We always work on the normalised, canonical form
            type = registry.normalize(type);
        }
        while (type != null) {
            // Try finding a parser for the type
            SCSParser parser = map.get(type);
            if (parser != null) {
                return parser;
            }

            // Failing that, try for the parent of the type
            type = registry.getSupertype(type);
        }
        return fallback;
    }

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return getParsers(context).keySet();
    }

    public void parse(InputStream stream, ContentHandler handler,Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
        SCSParser parser = getParser(metadata, context);
        TemporaryResources tmp = new TemporaryResources();
        try {
            TikaInputStream taggedStream = TikaInputStream.get(stream, tmp);
            TaggedContentHandler taggedHandler =
                    handler != null ? new TaggedContentHandler(handler) : null;
            if (metadata.get("X-Parsed-By")!=null) {
                metadata.remove("X-Parsed-By");
            }
            if (parser instanceof SCSParserDecorator) {
                metadata.add("X-Parsed-By", ((SCSParserDecorator) parser).getWrappedParser().getClass().getName());
            } else {
                metadata.add("X-Parsed-By", parser.getClass().getName());
            }
            try {
                parser.parse(taggedStream, taggedHandler, metadata, context);
            } catch (RuntimeException e) {
                throw new TikaException(
                        "Unexpected RuntimeException from " + parser, e);
            } catch (IOException e) {
                taggedStream.throwIfCauseOf(e);
                throw new TikaException(
                        "TIKA-198: Illegal IOException from " + parser, e);
            } catch (SAXException e) {
                if (taggedHandler != null) taggedHandler.throwIfCauseOf(e);
                throw new TikaException(
                        "TIKA-237: Illegal SAXException from " + parser, e);
            }
        } finally {
            tmp.dispose();
        }
    }

}
