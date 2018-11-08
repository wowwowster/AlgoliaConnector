package com.sword.gsa.spis.scs.extracting.parser;

import org.apache.tika.detect.DefaultEncodingDetector;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.parser.ParseContext;


public abstract class SCSAbstractEncodingDetectorParser extends SCSAbstractParser {

    private EncodingDetector encodingDetector;

    public SCSAbstractEncodingDetectorParser() {
        encodingDetector = new DefaultEncodingDetector();
    }

    public SCSAbstractEncodingDetectorParser(EncodingDetector encodingDetector) {
        this.encodingDetector = encodingDetector;
    }

    protected EncodingDetector getEncodingDetector(ParseContext parseContext) {

        EncodingDetector fromParseContext = parseContext.get(EncodingDetector.class);
        if (fromParseContext != null) {
            return fromParseContext;
        }

        return getEncodingDetector();
    }

    public EncodingDetector getEncodingDetector() {
        return encodingDetector;
    }

    public void setEncodingDetector(EncodingDetector encodingDetector) {
        this.encodingDetector = encodingDetector;
    }
}

