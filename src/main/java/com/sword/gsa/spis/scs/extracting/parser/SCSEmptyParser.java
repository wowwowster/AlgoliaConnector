package com.sword.gsa.spis.scs.extracting.parser;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SCSEmptyParser extends SCSAbstractParser {

    private static final long serialVersionUID = -4218649699095732123L;

    private static final Logger logger = LoggerFactory.getLogger(SCSEmptyParser.class);

    /**
     * Singleton instance of this class.
     */
    public static final SCSEmptyParser INSTANCE = new SCSEmptyParser();

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return Collections.emptySet();
    }

    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws SAXException {
        // TODO @claurier voir si on peut renseigner le nom du document dans la log suivante
        logger.error(String.format("Document type not recognized, the parser by default is used"));
        XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();
        xhtml.endDocument();
    }
}

