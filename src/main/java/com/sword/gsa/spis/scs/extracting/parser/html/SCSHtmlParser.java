package com.sword.gsa.spis.scs.extracting.parser.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sword.gsa.spis.scs.extracting.parser.SCSAbstractEncodingDetectorParser;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.tika.config.Field;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.DefaultHtmlMapper;
import org.apache.tika.parser.html.HtmlMapper;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SCSHtmlParser extends SCSAbstractEncodingDetectorParser {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 7895315240498733128L;

    private static final Logger LOG = LoggerFactory.getLogger(SCSHtmlParser.class);

    private static final MediaType XHTML = MediaType.application("xhtml+xml");
    private static final MediaType WAP_XHTML = MediaType.application("vnd.wap.xhtml+xml");
    private static final MediaType X_ASP = MediaType.application("x-asp");

    private static final Set<MediaType> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    MediaType.text("html"),
                    XHTML,
                    WAP_XHTML,
                    X_ASP)));

    /**
     * HTML schema singleton used to amortise the heavy instantiation time.
     */
    private static final Schema HTML_SCHEMA = new HTMLSchema();

    @Field
    private boolean extractScripts = false;

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    public SCSHtmlParser() {
        super();
    }

    public SCSHtmlParser(EncodingDetector encodingDetector) {
        super(encodingDetector);
    }

    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {

        // Automatically detect the character encoding
        try (AutoDetectReader reader = new AutoDetectReader(new CloseShieldInputStream(stream),
                metadata, getEncodingDetector(context))) {
            Charset charset = reader.getCharset();
            String previous = metadata.get(Metadata.CONTENT_TYPE);
            MediaType contentType = null;
            if (previous == null || previous.startsWith("text/html")) {
                contentType = new MediaType(MediaType.TEXT_HTML, charset);
            } else if (previous.startsWith("application/xhtml+xml")) {
                contentType = new MediaType(XHTML, charset);
            } else if (previous.startsWith("application/vnd.wap.xhtml+xml")) {
                contentType = new MediaType(WAP_XHTML, charset);
            } else if (previous.startsWith("application/x-asp")) {
                contentType = new MediaType(X_ASP, charset);
            }
            if (contentType != null) {
                metadata.set(Metadata.CONTENT_TYPE, contentType.toString());
            }
            // deprecated, see TIKA-431
            metadata.set(Metadata.CONTENT_ENCODING, charset.name());

            // Get the HTML mapper from the parse context
            HtmlMapper mapper =
                    context.get(HtmlMapper.class, new HtmlParserMapper());

            // Parse the HTML document
            org.ccil.cowan.tagsoup.Parser parser =
                    new org.ccil.cowan.tagsoup.Parser();

            // Use schema from context or default
            Schema schema = context.get(Schema.class, HTML_SCHEMA);

            // TIKA-528: Reuse share schema to avoid heavy instantiation
            parser.setProperty(
                    org.ccil.cowan.tagsoup.Parser.schemaProperty, schema);
            // TIKA-599: Shared schema is thread-safe only if bogons are ignored
            parser.setFeature(
                    org.ccil.cowan.tagsoup.Parser.ignoreBogonsFeature, true);

            parser.setContentHandler(new SCSXHTMLDowngradeHandler(
                    new SCSHtmlHandler(mapper, handler, metadata, context, extractScripts)));

            parser.parse(reader.asInputSource());
        }
    }

    protected String mapSafeElement(String name) {
        return DefaultHtmlMapper.INSTANCE.mapSafeElement(name);
    }

    /**
     * Checks whether all content within the given HTML element should be
     * discarded instead of including it in the parse output. Subclasses
     * can override this method to customize the set of discarded elements.
     *
     * @param name HTML element name (upper case)
     * @return <code>true</code> if content inside the named element
     * should be ignored, <code>false</code> otherwise
     * @since Apache Tika 0.5
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This method will be removed in Tika 1.0.
     */
    protected boolean isDiscardElement(String name) {
        return DefaultHtmlMapper.INSTANCE.isDiscardElement(name);
    }

    /**
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This method will be removed in Tika 1.0.
     */
    public String mapSafeAttribute(String elementName, String attributeName) {
        return DefaultHtmlMapper.INSTANCE.mapSafeAttribute(elementName, attributeName);
    }

    /**
     * Adapter class that maintains backwards compatibility with the
     * protected SCSHtmlParser methods. Making SCSHtmlParser implement HtmlMapper
     * directly would require those methods to be public, which would break
     * backwards compatibility with subclasses.
     *
     * @deprecated Use the {@link HtmlMapper} mechanism to customize
     * the HTML mapping. This class will be removed in Tika 1.0.
     */
    private class HtmlParserMapper implements HtmlMapper {
        public String mapSafeElement(String name) {
            return SCSHtmlParser.this.mapSafeElement(name);
        }

        public boolean isDiscardElement(String name) {
            return SCSHtmlParser.this.isDiscardElement(name);
        }

        public String mapSafeAttribute(String elementName, String attributeName) {
            return SCSHtmlParser.this.mapSafeAttribute(elementName, attributeName);
        }
    }

    /**
     * Whether or not to extract contents in script entities.
     * Default is <code>false</code>
     *
     * @param extractScripts
     */
    @Field
    public void setExtractScripts(boolean extractScripts) {
        this.extractScripts = extractScripts;
    }

    public boolean getExtractScripts() {
        return extractScripts;
    }

}
