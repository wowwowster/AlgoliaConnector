package com.sword.gsa.spis.scs.extracting.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SCSParserDecorator extends SCSAbstractParser {


    private static final long serialVersionUID = -3861669115439125268L;
    private final SCSParser parser;

    public static final SCSParser withTypes(SCSParser parser, final Set<MediaType> types) {
        return new SCSParserDecorator(parser) {
            private static final long serialVersionUID = -7345051519565330731L;

            public Set<MediaType> getSupportedTypes(ParseContext context) {
                return types;
            }

            public String getDecorationName() {
                return "With Types";
            }
        };
    }

    public static final SCSParser withoutTypes(SCSParser parser, final Set<MediaType> excludeTypes) {
        return new SCSParserDecorator(parser) {
            private static final long serialVersionUID = 7979614774021768609L;

            public Set<MediaType> getSupportedTypes(ParseContext context) {
                Set<MediaType> parserTypes = new HashSet(super.getSupportedTypes(context));
                parserTypes.removeAll(excludeTypes);
                return parserTypes;
            }

            public String getDecorationName() {
                return "Without Types";
            }
        };
    }

    /** @deprecated */
    public static final SCSParser withFallbacks(final Collection<? extends SCSParser> parsers, final Set<MediaType> types) {
        SCSParser parser = SCSEmptyParser.INSTANCE;
        if(!parsers.isEmpty()) {
            parser = (SCSParser)parsers.iterator().next();
        }

        return new SCSParserDecorator((SCSParser)parser) {
            private static final long serialVersionUID = 1625187131782069683L;

            public Set<MediaType> getSupportedTypes(ParseContext context) {
                return types;
            }

            public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
                TemporaryResources tmp = TikaInputStream.isTikaInputStream(stream)?null:new TemporaryResources();

                try {
                    TikaInputStream tstream = TikaInputStream.get(stream, tmp);
                    tstream.getFile();
                    Iterator var7 = parsers.iterator();

                    while(var7.hasNext()) {
                        SCSParser p = (SCSParser)var7.next();
                        tstream.mark(-1);

                        try {
                            p.parse(tstream, handler, metadata, context);
                            return;
                        } catch (Exception var13) {
                            tstream.reset();
                        }
                    }
                } finally {
                    if(tmp != null) {
                        tmp.dispose();
                    }

                }

            }

            public String getDecorationName() {
                return "With Fallback";
            }
        };
    }

    public SCSParserDecorator(SCSParser parser) {
        this.parser = parser;
    }

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return this.parser.getSupportedTypes(context);
    }

    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
        this.parser.parse(stream, handler, metadata, context);
    }

    public String getDecorationName() {
        return null;
    }

    public SCSParser getWrappedParser() {
        return this.parser;
    }
}

