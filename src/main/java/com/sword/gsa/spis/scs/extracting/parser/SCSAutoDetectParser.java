package com.sword.gsa.spis.scs.extracting.parser;

import java.io.IOException;
import java.io.InputStream;

import com.sword.gsa.spis.scs.extracting.config.ExtractionConfig;
import com.sword.gsa.spis.scs.extracting.detect.SCSDefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.ZeroByteFileException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.SecureContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SCSAutoDetectParser extends SCSCompositeParser {

    private static final long serialVersionUID = 6110455808615143122L;
    // private final TikaConfig config;

    /**
     * The type detector used by this parser to auto-detect the type of a document.
     */
    private Detector detector; // always set in the constructor

    /**
     * Creates an auto-detecting parser instance using the default Tika
     * configuration.
     */
    public SCSAutoDetectParser() {
        this(ExtractionConfig.getDefaultConfig());
    }

    public SCSAutoDetectParser(Detector detector) {
        this(ExtractionConfig.getDefaultConfig());
        setDetector(detector);
    }

    public SCSAutoDetectParser(SCSParser... parsers) {
        this(new SCSDefaultDetector(), parsers);
    }

    public SCSAutoDetectParser(Detector detector, SCSParser... parsers) {
        super(MediaTypeRegistry.getDefaultRegistry(), parsers);
        setDetector(detector);
    }

    public SCSAutoDetectParser(ExtractionConfig config) {
        super(config.getMediaTypeRegistry(), config.getParser());
        setDetector(config.getDetector());
    }

    /**
     * Returns the type detector used by this parser to auto-detect the type of a
     * document.
     *
     * @return type detector
     * @since Apache Tika 0.4
     */
    public Detector getDetector() {
        return detector;
    }

    /**
     * Sets the type detector used by this parser to auto-detect the type of a
     * document.
     *
     * @param detector
     *            type detector
     * @since Apache Tika 0.4
     */
    public void setDetector(Detector detector) {
        this.detector = detector;
    }

    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        TemporaryResources tmp = new TemporaryResources();
        try {
            TikaInputStream tis = TikaInputStream.get(stream, tmp);

            // Automatically detect the MIME type of the document
            MediaType type = detector.detect(tis, metadata);
            metadata.set(Metadata.CONTENT_TYPE, type.toString());
            // check for zero-byte inputstream
            if (tis.getOpenContainer() == null) {
                tis.mark(1);
                if (tis.read() == -1) {
                    throw new ZeroByteFileException("InputStream must have > 0 bytes");
                }
                tis.reset();
            }
            // TIKA-216: Zip bomb prevention
            SecureContentHandler sch = handler != null ? new SecureContentHandler(handler, tis) : null;

            // pass self to handle embedded documents if
            // the caller hasn't specified one.
            if (context.get(EmbeddedDocumentExtractor.class) == null) {
                SCSParser p = context.get(SCSParser.class);
                if (p == null) {
                    context.set(SCSParser.class, this);
                }
                context.set(EmbeddedDocumentExtractor.class, new ParsingEmbeddedDocumentExtractor(context));
            }

            try {
                // Parse the document
                super.parse(tis, sch, metadata, context);
            } catch (SAXException e) {
                // Convert zip bomb exceptions to TikaExceptions
                sch.throwIfCauseOf(e);
                throw e;
            }
        } finally {
            tmp.dispose();
        }
    }

    public void parse(InputStream stream, ContentHandler handler, Metadata metadata)
            throws IOException, SAXException, TikaException {
        ParseContext context = new ParseContext();
        context.set(SCSParser.class, this);
        parse(stream, handler, metadata, context);
    }

}
