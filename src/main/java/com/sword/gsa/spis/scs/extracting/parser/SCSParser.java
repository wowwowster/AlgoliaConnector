package com.sword.gsa.spis.scs.extracting.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface SCSParser extends Serializable {

    Set<MediaType> getSupportedTypes(ParseContext context);

    void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException;

}
