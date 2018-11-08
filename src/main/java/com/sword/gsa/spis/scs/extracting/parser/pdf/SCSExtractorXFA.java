package com.sword.gsa.spis.scs.extracting.parser.pdf;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SCSExtractorXFA {
        private static final Pattern XFA_TEMPLATE_ANY_VERSION = Pattern.compile("^http://www.xfa.org/schema/xfa-template");
        private static final Pattern TEXT_PATTERN = Pattern.compile("^(speak|text|contents-richtext|toolTip|exData)$");
        private static final String XFA_DATA_NS = "http://www.xfa.org/schema/xfa-data/1.0/";
        private static final String FIELD_LN = "field";
        private static final QName XFA_DATA = new QName("http://www.xfa.org/schema/xfa-data/1.0/", "data");
        private final Matcher xfaTemplateMatcher;
        private final Matcher textMatcher;

        SCSExtractorXFA() {
            this.xfaTemplateMatcher = XFA_TEMPLATE_ANY_VERSION.matcher("");
            this.textMatcher = TEXT_PATTERN.matcher("");
        }

        void extract(InputStream xfaIs, XHTMLContentHandler xhtml, Metadata m, ParseContext context) throws XMLStreamException, SAXException {
            xhtml.startElement("div", "class", "xfa_content");
            Map<String, String> pdfObjRToValues = new HashMap();
            Map<String, XFAField> namedFields = new LinkedHashMap();
            XMLStreamReader reader = context.getXMLInputFactory().createXMLStreamReader(xfaIs);

            while(true) {
                while(true) {
                    while(reader.hasNext()) {
                        switch(reader.next()) {
                            case 1:
                                QName name = reader.getName();
                                String localName = name.getLocalPart();
                                if(this.xfaTemplateMatcher.reset(name.getNamespaceURI()).find() && "field".equals(name.getLocalPart())) {
                                    this.handleField(reader, namedFields);
                                } else if(XFA_DATA.equals(name)) {
                                    this.loadData(reader, pdfObjRToValues);
                                } else if(this.textMatcher.reset(localName).find()) {
                                    this.scrapeTextUntil(reader, xhtml, name);
                                }
                            case 2:
                        }
                    }

                    if(namedFields.size() == 0) {
                        xhtml.endElement("xfa_content");
                        return;
                    }

                    xhtml.startElement("div", "class", "xfa_form");
                    xhtml.startElement("ol");
                    StringBuilder sb = new StringBuilder();
                    Iterator var17 = namedFields.entrySet().iterator();

                    while(var17.hasNext()) {
                        Map.Entry<String, XFAField> e = (Map.Entry)var17.next();
                        String fieldName = (String)e.getKey();
                        XFAField field = (XFAField)e.getValue();
                        String fieldValue = (String)pdfObjRToValues.get(fieldName);
                        AttributesImpl attrs = new AttributesImpl();
                        attrs.addAttribute("", "fieldName", "fieldName", "CDATA", fieldName);
                        String displayFieldName = field.toolTip != null && field.toolTip.trim().length() != 0?field.toolTip:fieldName;
                        sb.append(displayFieldName).append(": ");
                        if(fieldValue != null) {
                            sb.append(fieldValue);
                        }

                        xhtml.startElement("li", attrs);
                        xhtml.characters(sb.toString());
                        xhtml.endElement("li");
                        sb.setLength(0);
                    }

                    xhtml.endElement("ol");
                    xhtml.endElement("div");
                    xhtml.endElement("xfa_content");
                    return;
                }
            }
        }

        private void scrapeTextUntil(XMLStreamReader reader, XHTMLContentHandler xhtml, QName endElement) throws XMLStreamException, SAXException {
            StringBuilder buffer = new StringBuilder();
            boolean keepGoing = true;

            while(reader.hasNext() && keepGoing) {
                int start;
                int length;
                switch(reader.next()) {
                    case 1:
                    default:
                        break;
                    case 2:
                        if(reader.getName().equals(endElement)) {
                            keepGoing = false;
                        } else if("p".equals(reader.getName().getLocalPart())) {
                            xhtml.element("p", buffer.toString());
                            buffer.setLength(0);
                        }
                        break;
                    case 4:
                        start = reader.getTextStart();
                        length = reader.getTextLength();
                        buffer.append(reader.getTextCharacters(), start, length);
                        break;
                    case 12:
                        start = reader.getTextStart();
                        length = reader.getTextLength();
                        buffer.append(reader.getTextCharacters(), start, length);
                }
            }

            String remainder = buffer.toString();
            if(remainder.trim().length() > 0) {
                xhtml.element("p", remainder);
            }

        }

        private String scrapeTextUntil(XMLStreamReader reader, QName endElement) throws XMLStreamException {
            StringBuilder buffer = new StringBuilder();
            boolean keepGoing = true;

            while(reader.hasNext() && keepGoing) {
                int start;
                int length;
                switch(reader.next()) {
                    case 1:
                    default:
                        break;
                    case 2:
                        if(reader.getName().equals(endElement)) {
                            keepGoing = false;
                        } else if("p".equals(reader.getName().getLocalPart())) {
                            buffer.append("\n");
                        }
                        break;
                    case 4:
                        start = reader.getTextStart();
                        length = reader.getTextLength();
                        buffer.append(reader.getTextCharacters(), start, length);
                        break;
                    case 12:
                        start = reader.getTextStart();
                        length = reader.getTextLength();
                        buffer.append(reader.getTextCharacters(), start, length);
                }
            }

            return buffer.toString();
        }

        private void loadData(XMLStreamReader reader, Map<String, String> pdfObjRToValues) throws XMLStreamException {
            StringBuilder buffer = new StringBuilder();

            while(reader.hasNext()) {
                int start;
                int length;
                switch(reader.next()) {
                    case 1:
                    default:
                        break;
                    case 2:
                        if(buffer.length() > 0) {
                            String localName = reader.getLocalName();
                            pdfObjRToValues.put(localName, buffer.toString());
                            buffer.setLength(0);
                        }

                        if(XFA_DATA.equals(reader.getName())) {
                            return;
                        }
                        break;
                    case 4:
                        start = reader.getTextStart();
                        length = reader.getTextLength();
                        buffer.append(reader.getTextCharacters(), start, length);
                        break;
                    case 12:
                        start = reader.getTextStart();
                        length = reader.getTextLength();
                        buffer.append(reader.getTextCharacters(), start, length);
                }
            }

        }

        private void handleField(XMLStreamReader reader, Map<String, XFAField> fields) throws XMLStreamException {
            String fieldName = this.findFirstAttributeValue(reader, "name");
            String pdfObjRef = "";
            String toolTip = "";

            while(reader.hasNext()) {
                switch(reader.next()) {
                    case 1:
                        if("toolTip".equals(reader.getName().getLocalPart())) {
                            toolTip = this.scrapeTextUntil(reader, reader.getName());
                        }
                        break;
                    case 2:
                        if(this.xfaTemplateMatcher.reset(reader.getName().getNamespaceURI()).find() && "field".equals(reader.getName().getLocalPart())) {
                            if(fieldName != null) {
                                fields.put(fieldName, new XFAField(fieldName, toolTip, pdfObjRef));
                            }

                            return;
                        }
                        break;
                    case 3:
                        if("PDF_OBJR".equals(reader.getPITarget())) {
                            pdfObjRef = reader.getPIData();
                        }
                }
            }

        }

        private String findFirstAttributeValue(XMLStreamReader reader, String name) {
            for(int i = 0; i < reader.getAttributeCount(); ++i) {
                String n = reader.getAttributeLocalName(i);
                if(name.equals(n)) {
                    return reader.getAttributeValue(i);
                }
            }

            return "";
        }

        class XFAField {
            String fieldName;
            String toolTip;
            String pdfObjRef;
            String value;

            public XFAField(String fieldName, String toolTip, String pdfObjRef) {
                this.fieldName = fieldName;
                this.toolTip = toolTip;
                this.pdfObjRef = pdfObjRef;
            }

            public String toString() {
                return "XFAField{fieldName='" + this.fieldName + '\'' + ", toolTip='" + this.toolTip + '\'' + ", pdfObjRef='" + this.pdfObjRef + '\'' + ", value='" + this.value + '\'' + '}';
            }
        }
    }

