package com.sword.gsa.spis.scs.extracting;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.sword.gsa.spis.scs.extracting.parser.SCSAutoDetectParser;
import com.sword.gsa.spis.scs.extracting.parser.html.SCSHtmlParser;
import com.sword.gsa.spis.scs.extracting.parser.xml.SCSXMLParser;
import com.sword.gsa.spis.scs.push.throwables.DoNotIndex;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;


public abstract class AParsingHandler implements AutoCloseable {

    protected static final int MAXIMUM_TEXT_CHUNK_SIZE = 143;
    protected static final Logger logger = Logger.getLogger(AParsingHandler.class);
    protected static Property PARAGRAPH = Property.internalTextBag("paragraph");
    protected static SCSAutoDetectParser parser = new SCSAutoDetectParser();

    public AParsingHandler() {
        super();
    }

    public static List<String> parseXML(InputStream input) throws IOException, SAXException, TikaException {
        List<String> paragraphs = new ArrayList<String>();
        Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler(-1);
        new SCSXMLParser().parse(input, handler, metadata, new ParseContext());
        int indexMax = metadata.getValues(PARAGRAPH).length;

        for (int index = 0; index < indexMax; index++) {
            String paragraphContent = metadata.getValues(PARAGRAPH)[index];
            paragraphs.add(StringEscapeUtils.unescapeHtml4(paragraphContent.replaceAll("\n\\s++", " ")));
        }
        input.close();
        return paragraphs;
    }

    public static List<String> parseXML(String objectXMLContent) throws IOException, SAXException, TikaException {
        return parseXML(new ByteArrayInputStream(objectXMLContent.getBytes(StandardCharsets.UTF_8)));
    }

    public static String parseInputStreamToTextBlocks(String filename) throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToXMLContentHandler();

        SCSAutoDetectParser parser = new SCSAutoDetectParser();
        Metadata metadata = new Metadata();
        ClassPathResource resource = new ClassPathResource(filename);
        try (FileInputStream stream = new FileInputStream(resource.getFile())) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        }
    }

    public static String parseHTMLInputStreamToString(InputStream inputStream, Metadata metadata) throws IOException, SAXException, TikaException {
        ContentHandler body = new BodyContentHandler(-1);
        final StringWriter href = new StringWriter();
        final StringWriter name = new StringWriter();
        ContentHandler link = new DefaultHandler() {
            @Override
            public void startElement(String u, String l, String n, Attributes a) throws SAXException {
                if ("a".equals(l)) {
                    if (a.getValue("href") != null) {
                        href.append(a.getValue("href"));
                    } else if (a.getValue("name") != null) {
                        name.append(a.getValue("name"));
                    }
                }
            }
        };
        new SCSHtmlParser().parse(inputStream, new TeeContentHandler(body, link), metadata, new ParseContext());

        return body.toString();
    }

    public abstract List<TextBlockDTO> parseInputStreamToTextBlocks(InputStream inputStream, Metadata tikaMetadata)
        throws IOException, SAXException, TikaException, DoNotIndex ;

    public static List<String> parseToPlainTextChunks(String filename) throws IOException, SAXException, TikaException {
        final List<String> chunks = new ArrayList<>();
        chunks.add("");
        ContentHandlerDecorator handler = new ContentHandlerDecorator() {
            @Override
            public void characters(char[] ch, int start, int length) {
                String lastChunk = chunks.get(chunks.size() - 1);
                String thisStr = new String(ch, start, length);

                if (lastChunk.length() + length > MAXIMUM_TEXT_CHUNK_SIZE) {
                    chunks.add(thisStr);
                } else {
                    chunks.set(chunks.size() - 1, lastChunk + thisStr);
                }
            }
        };

        SCSAutoDetectParser parser = new SCSAutoDetectParser();
        Metadata metadata = new Metadata();
        ClassPathResource resource = new ClassPathResource(filename);
        try (FileInputStream stream = new FileInputStream(resource.getFile())) {
            // try (InputStream stream =
            // TikaContentHandler.class.getResourceAsStream(filename)) {
            parser.parse(stream, handler, metadata);
            return chunks;
        }
    }

}
