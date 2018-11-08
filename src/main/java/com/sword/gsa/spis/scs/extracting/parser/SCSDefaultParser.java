package com.sword.gsa.spis.scs.extracting.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import com.sword.gsa.spis.scs.extracting.detect.SCSDefaultEncodingDetector;
import com.sword.gsa.spis.scs.extracting.config.SCSServiceLoader;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.AbstractEncodingDetectorParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.utils.ServiceLoaderUtils;


public class SCSDefaultParser extends SCSCompositeParser {

    private static final long serialVersionUID = 3612324825403757520L;

    private static List<SCSParser> getDefaultParsers(SCSServiceLoader loader,
                                                     EncodingDetector encodingDetector) {
        List<SCSParser> parsers = loader.loadStaticServiceProviders(SCSParser.class);

        if (encodingDetector != null) {
            for (SCSParser p : parsers) {
                setEncodingDetector(p, encodingDetector);
            }
        }
        ServiceLoaderUtils.sortLoadedClasses(parsers);
        return parsers;
    }

    //recursively go through the parsers and set the encoding detector
    //as configured in the config file
    private static void setEncodingDetector(SCSParser p, EncodingDetector encodingDetector) {
        if (p instanceof AbstractEncodingDetectorParser) {
            ((AbstractEncodingDetectorParser)p).setEncodingDetector(encodingDetector);
        } else if (p instanceof SCSCompositeParser) {
            for (SCSParser child : ((SCSCompositeParser)p).getAllComponentParsers()) {
                setEncodingDetector(child, encodingDetector);
            }
        } else if (p instanceof SCSParserDecorator) {
            setEncodingDetector(((SCSParserDecorator)p).getWrappedParser(), encodingDetector);
        }
    }

    private transient final SCSServiceLoader loader;

    public SCSDefaultParser(MediaTypeRegistry registry, SCSServiceLoader loader,
                            Collection<Class<? extends SCSParser>> excludeParsers,
                            EncodingDetector encodingDetector) {
        super(registry, getDefaultParsers(loader, encodingDetector), excludeParsers);
        this.loader = loader;
    }

    public SCSDefaultParser(MediaTypeRegistry registry, SCSServiceLoader loader,
                            Collection<Class<? extends SCSParser>> excludeParsers) {
        super(registry, getDefaultParsers(loader, new SCSDefaultEncodingDetector(loader)), excludeParsers);
        this.loader = loader;
    }

    public SCSDefaultParser(MediaTypeRegistry registry, SCSServiceLoader loader, EncodingDetector encodingDetector) {
        this(registry, loader, null, encodingDetector);
    }

    public SCSDefaultParser(MediaTypeRegistry registry, SCSServiceLoader loader) {
        this(registry, loader, null, new SCSDefaultEncodingDetector(loader));
    }

    public SCSDefaultParser(MediaTypeRegistry registry, ClassLoader loader) {
        this(registry, new SCSServiceLoader(loader));
    }

    public SCSDefaultParser(ClassLoader loader) {
        this(MediaTypeRegistry.getDefaultRegistry(), new SCSServiceLoader(loader));
    }

    public SCSDefaultParser(MediaTypeRegistry registry) {
        this(registry, new SCSServiceLoader());
    }

    public SCSDefaultParser() {
        this(MediaTypeRegistry.getDefaultRegistry());
    }

    @Override
    public Map<MediaType, SCSParser> getParsers(ParseContext context) {
        Map<MediaType, SCSParser> map = super.getParsers(context);

        if (loader != null) {
            // Add dynamic parser service (they always override static ones)
            MediaTypeRegistry registry = getMediaTypeRegistry();
            List<SCSParser> parsers =
                    loader.loadDynamicServiceProviders(SCSParser.class);
            Collections.reverse(parsers); // best parser last
            for (SCSParser parser : parsers) {
                for (MediaType type : parser.getSupportedTypes(context)) {
                    map.put(registry.normalize(type), parser);
                }
            }
        }

        return map;
    }

    @Override
    public List<SCSParser> getAllComponentParsers() {
        List<SCSParser> parsers = super.getAllComponentParsers();
        if (loader != null) {
            parsers = new ArrayList<SCSParser>(parsers);
            parsers.addAll(loader.loadDynamicServiceProviders(SCSParser.class));
        }
        return parsers;
    }
}

