package com.sword.gsa.spis.scs.extracting.parser.html;

import javax.xml.XMLConstants;
import java.util.Locale;

import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SCSXHTMLDowngradeHandler extends ContentHandlerDecorator {

    public SCSXHTMLDowngradeHandler(ContentHandler handler) {
        super(handler);
    }

    @Override
    public void startElement(
            String uri, String localName, String name, Attributes atts)
            throws SAXException {
        String upper = localName.toUpperCase(Locale.ENGLISH);

        AttributesImpl attributes = new AttributesImpl();
        for (int i = 0; i < atts.getLength(); i++) {
            String auri = atts.getURI(i);
            String local = atts.getLocalName(i);
            String qname = atts.getQName(i);
            if (XMLConstants.NULL_NS_URI.equals(auri)
                    && !local.equals(XMLConstants.XMLNS_ATTRIBUTE)
                    && !qname.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")) {
                attributes.addAttribute(
                        auri, local, qname, atts.getType(i), atts.getValue(i));
            }
        }

        super.startElement(XMLConstants.NULL_NS_URI, upper, upper, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        String upper = localName.toUpperCase(Locale.ENGLISH);
        super.endElement(XMLConstants.NULL_NS_URI, upper, upper);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }

}

