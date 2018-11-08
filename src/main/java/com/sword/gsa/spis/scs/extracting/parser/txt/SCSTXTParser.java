package com.sword.gsa.spis.scs.extracting.parser.txt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import com.sword.gsa.spis.scs.extracting.parser.SCSAbstractEncodingDetectorParser;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SCSTXTParser extends SCSAbstractEncodingDetectorParser {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -6656102320836888910L;

    private static final Set<MediaType> SUPPORTED_TYPES =
            Collections.singleton(MediaType.TEXT_PLAIN);

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    public SCSTXTParser() {
        super();
    }

    public SCSTXTParser(EncodingDetector encodingDetector) {
        super(encodingDetector);
    }

    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {

        // Automatically detect the character encoding
        try (AutoDetectReader reader = new AutoDetectReader(
                new CloseShieldInputStream(stream), metadata, getEncodingDetector(context))) {
            //try to get detected content type; could be a subclass of text/plain
            //such as vcal, etc.
            String incomingMime = metadata.get(Metadata.CONTENT_TYPE);
            MediaType mediaType = MediaType.TEXT_PLAIN;
            if (incomingMime != null) {
                MediaType tmpMediaType = MediaType.parse(incomingMime);
                if (tmpMediaType != null) {
                    mediaType = tmpMediaType;
                }
            }
            Charset charset = reader.getCharset();
            MediaType type = new MediaType(mediaType, charset);
            metadata.set(Metadata.CONTENT_TYPE, type.toString());
            // deprecated, see TIKA-431
            metadata.set(Metadata.CONTENT_ENCODING, charset.name());

            XHTMLContentHandler xhtml =
                    new XHTMLContentHandler(handler, metadata);
            xhtml.startDocument();

            xhtml.startElement("p");
            char[] buffer = new char[4096];
            int n = reader.read(buffer);
            while (n != -1) {
                xhtml.characters(buffer, 0, n);
                n = reader.read(buffer);
            }
            xhtml.endElement("p");

            xhtml.endDocument();
        }
    }

}

