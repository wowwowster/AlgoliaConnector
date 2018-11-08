package com.sword.gsa.spis.scs.extracting.parser.pdf;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.stream.XMLStreamException;

import com.sword.gsa.spis.scs.extracting.pdfbox.text.SCSPDFTextStripper;
import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDSimpleFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionImportData;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionLaunch;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionRemoteGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.action.PDAnnotationAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.action.PDPageAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.form.PDXFAResource;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

    public     class SCSAbstractPDF2XHTML extends SCSPDFTextStripper {
        private static final int MAX_ACROFORM_RECURSIONS = 10;
        private static final TesseractOCRConfig DEFAULT_TESSERACT_CONFIG = new TesseractOCRConfig();
        private final SimpleDateFormat dateFormat;
        final List<IOException> exceptions;
        final PDDocument pdDocument;
        final XHTMLContentHandler xhtml;
        private final ParseContext context;
        final Metadata metadata;
        final EmbeddedDocumentExtractor embeddedDocumentExtractor;
        final SCSParserPDFConfig config;
        private int pageIndex;

        SCSAbstractPDF2XHTML(PDDocument pdDocument, ContentHandler handler, ParseContext context, Metadata metadata, SCSParserPDFConfig config) throws IOException {
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
            this.exceptions = new ArrayList();
            this.pageIndex = 0;
            this.pdDocument = pdDocument;
            this.xhtml = new XHTMLContentHandler(handler, metadata);
            this.context = context;
            this.metadata = metadata;
            this.config = config;
            this.embeddedDocumentExtractor = EmbeddedDocumentUtil.getEmbeddedDocumentExtractor(context);
        }

        protected void startPage(PDPage page) throws IOException {
            try {
                this.xhtml.startElement("div", "class", "page");
            } catch (SAXException var3) {
                throw new IOExceptionWithCause("Unable to start a page", var3);
            }

            this.writeParagraphStart();
        }

        private void extractEmbeddedDocuments(PDDocument document) throws IOException, SAXException, TikaException {
            PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(document.getDocumentCatalog());
            PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
            if(efTree != null) {
                Map<String, PDComplexFileSpecification> embeddedFileNames = efTree.getNames();
                if(embeddedFileNames != null) {
                    this.processEmbeddedDocNames(embeddedFileNames);
                } else {
                    List<PDNameTreeNode<PDComplexFileSpecification>> kids = efTree.getKids();
                    if(kids == null) {
                        return;
                    }

                    Iterator var6 = kids.iterator();

                    while(var6.hasNext()) {
                        PDNameTreeNode<PDComplexFileSpecification> node = (PDNameTreeNode)var6.next();
                        embeddedFileNames = node.getNames();
                        if(embeddedFileNames != null) {
                            this.processEmbeddedDocNames(embeddedFileNames);
                        }
                    }
                }

            }
        }

        private void processDoc(String name, PDFileSpecification spec, AttributesImpl attributes) throws TikaException, SAXException, IOException {
            if(spec instanceof PDSimpleFileSpecification) {
                attributes.addAttribute("", "class", "class", "CDATA", "linked");
                attributes.addAttribute("", "id", "id", "CDATA", spec.getFile());
                this.xhtml.startElement("div", attributes);
                this.xhtml.endElement("div");
            } else if(spec instanceof PDComplexFileSpecification) {
                if(attributes.getIndex("source") < 0) {
                    attributes.addAttribute("", "source", "source", "CDATA", "attachment");
                }

                this.extractMultiOSPDEmbeddedFiles(name, (PDComplexFileSpecification)spec, attributes);
            }

        }

        private void processEmbeddedDocNames(Map<String, PDComplexFileSpecification> embeddedFileNames) throws IOException, SAXException, TikaException {
            if(embeddedFileNames != null && !embeddedFileNames.isEmpty()) {
                Iterator var2 = embeddedFileNames.entrySet().iterator();

                while(var2.hasNext()) {
                    Map.Entry<String, PDComplexFileSpecification> ent = (Map.Entry)var2.next();
                    this.processDoc((String)ent.getKey(), (PDFileSpecification)ent.getValue(), new AttributesImpl());
                }

            }
        }

        private void extractMultiOSPDEmbeddedFiles(String displayName, PDComplexFileSpecification spec, AttributesImpl attributes) throws IOException, SAXException, TikaException {
            if(spec != null) {
                this.extractPDEmbeddedFile(displayName, spec.getFileUnicode(), spec.getFile(), spec.getEmbeddedFile(), attributes);
                this.extractPDEmbeddedFile(displayName, spec.getFileUnicode(), spec.getFileMac(), spec.getEmbeddedFileMac(), attributes);
                this.extractPDEmbeddedFile(displayName, spec.getFileUnicode(), spec.getFileDos(), spec.getEmbeddedFileDos(), attributes);
                this.extractPDEmbeddedFile(displayName, spec.getFileUnicode(), spec.getFileUnix(), spec.getEmbeddedFileUnix(), attributes);
            }
        }

        private void extractPDEmbeddedFile(String displayName, String unicodeFileName, String fileName, PDEmbeddedFile file, AttributesImpl attributes) throws SAXException, IOException, TikaException {
            if(file != null) {
                fileName = fileName != null && !"".equals(fileName.trim())?fileName:unicodeFileName;
                fileName = fileName != null && !"".equals(fileName.trim())?fileName:displayName;
                Metadata embeddedMetadata = new Metadata();
                embeddedMetadata.set("resourceName", fileName);
                embeddedMetadata.set("Content-Type", file.getSubtype());
                embeddedMetadata.set("Content-Length", Long.toString((long)file.getSize()));
                embeddedMetadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_TYPE, TikaCoreProperties.EmbeddedResourceType.ATTACHMENT.toString());
                embeddedMetadata.set(TikaCoreProperties.ORIGINAL_RESOURCE_NAME, fileName);
                if(this.embeddedDocumentExtractor.shouldParseEmbedded(embeddedMetadata)) {
                    TikaInputStream stream = null;

                    try {
                        stream = TikaInputStream.get(file.createInputStream());
                    } catch (IOException var13) {
                        EmbeddedDocumentUtil.recordEmbeddedStreamException(var13, this.metadata);
                        return;
                    }

                    try {
                        this.embeddedDocumentExtractor.parseEmbedded(stream, new EmbeddedContentHandler(this.xhtml), embeddedMetadata, false);
                        attributes.addAttribute("", "class", "class", "CDATA", "embedded");
                        attributes.addAttribute("", "id", "id", "CDATA", fileName);
                        this.xhtml.startElement("div", attributes);
                        this.xhtml.endElement("div");
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }

                }
            }
        }

        void handleCatchableIOE(IOException e) throws IOException {
            if(this.config.isCatchIntermediateIOExceptions()) {
                if(e.getCause() instanceof SAXException && e.getCause().getMessage() != null && e.getCause().getMessage().contains("Your document contained more than")) {
                    throw e;
                } else {
                    String msg = e.getMessage();
                    if(msg == null) {
                        msg = "IOException, no message";
                    }

                    this.metadata.add(TikaCoreProperties.TIKA_META_EXCEPTION_WARNING, msg);
                    this.exceptions.add(e);
                }
            } else {
                throw e;
            }
        }

        void doOCROnCurrentPage() throws IOException, TikaException, SAXException {
            if(!this.config.getOcrStrategy().equals(SCSParserPDFConfig.OCR_STRATEGY.NO_OCR)) {
                TesseractOCRConfig tesseractConfig = (TesseractOCRConfig)this.context.get(TesseractOCRConfig.class, DEFAULT_TESSERACT_CONFIG);
                TesseractOCRParser tesseractOCRParser = new TesseractOCRParser();
                if(!tesseractOCRParser.hasTesseract(tesseractConfig)) {
                    throw new TikaException("Tesseract is not available. Please set the OCR_STRATEGY to NO_OCR or configure Tesseract correctly");
                } else {
                    PDFRenderer renderer = new PDFRenderer(this.pdDocument);
                    TemporaryResources tmp = new TemporaryResources();

                    try {
                        BufferedImage image = renderer.renderImage(this.pageIndex, this.config.getOcrImageScale(), this.config.getOcrImageType());
                        Path tmpFile = tmp.createTempFile();
                        OutputStream os = Files.newOutputStream(tmpFile, new OpenOption[0]);
                        Throwable var8 = null;

                        try {
                            ImageIOUtil.writeImage(image, this.config.getOcrImageFormatName(), os, this.config.getOcrDPI(), this.config.getOcrImageQuality());
                        } catch (Throwable var50) {
                            var8 = var50;
                            throw var50;
                        } finally {
                            if(os != null) {
                                if(var8 != null) {
                                    try {
                                        os.close();
                                    } catch (Throwable var48) {
                                        var8.addSuppressed(var48);
                                    }
                                } else {
                                    os.close();
                                }
                            }

                        }

                        InputStream is = TikaInputStream.get(tmpFile);
                        var8 = null;

                        try {
                            tesseractOCRParser.parseInline(is, this.xhtml, tesseractConfig);
                        } catch (Throwable var49) {
                            var8 = var49;
                            throw var49;
                        } finally {
                            if(is != null) {
                                if(var8 != null) {
                                    try {
                                        is.close();
                                    } catch (Throwable var47) {
                                        var8.addSuppressed(var47);
                                    }
                                } else {
                                    is.close();
                                }
                            }

                        }
                    } catch (IOException var53) {
                        this.handleCatchableIOE(var53);
                    } catch (SAXException var54) {
                        throw new IOExceptionWithCause("error writing OCR content from PDF", var54);
                    } finally {
                        tmp.dispose();
                    }

                }
            }
        }

        protected void endPage(PDPage page) throws IOException {
            try {
                Iterator var2 = page.getAnnotations().iterator();

                while(var2.hasNext()) {
                    PDAnnotation annotation = (PDAnnotation)var2.next();
                    if(annotation instanceof PDAnnotationFileAttachment) {
                        PDAnnotationFileAttachment fann = (PDAnnotationFileAttachment)annotation;
                        PDComplexFileSpecification fileSpec = (PDComplexFileSpecification)fann.getFile();

                        try {
                            AttributesImpl attributes = new AttributesImpl();
                            attributes.addAttribute("", "source", "source", "CDATA", "annotation");
                            this.extractMultiOSPDEmbeddedFiles(fann.getAttachmentName(), fileSpec, attributes);
                        } catch (SAXException var16) {
                            throw new IOExceptionWithCause("file embedded in annotation sax exception", var16);
                        } catch (TikaException var17) {
                            throw new IOExceptionWithCause("file embedded in annotation extracting exception", var17);
                        } catch (IOException var18) {
                            this.handleCatchableIOE(var18);
                        }
                    } else if(annotation instanceof PDAnnotationWidget) {
                        this.handleWidget((PDAnnotationWidget)annotation);
                    }

                    if(this.config.getExtractAnnotationText()) {
                        PDActionURI uri = getActionURI(annotation);
                        if(uri != null) {
                            String link = uri.getURI();
                            if(link != null && link.trim().length() > 0) {
                                this.xhtml.startElement("div", "class", "annotation");
                                this.xhtml.startElement("a", "href", link);
                                this.xhtml.characters(link);
                                this.xhtml.endElement("a");
                                this.xhtml.endElement("div");
                            }
                        }

                        if(annotation instanceof PDAnnotationMarkup) {
                            PDAnnotationMarkup annotationMarkup = (PDAnnotationMarkup)annotation;
                            String title = annotationMarkup.getTitlePopup();
                            String subject = annotationMarkup.getSubject();
                            String contents = annotationMarkup.getContents();
                            if(title != null || subject != null || contents != null) {
                                this.xhtml.startElement("div", "class", "annotation");
                                if(title != null) {
                                    this.xhtml.startElement("div", "class", "annotationTitle");
                                    this.xhtml.characters(title);
                                    this.xhtml.endElement("div");
                                }

                                if(subject != null) {
                                    this.xhtml.startElement("div", "class", "annotationSubject");
                                    this.xhtml.characters(subject);
                                    this.xhtml.endElement("div");
                                }

                                if(contents != null) {
                                    this.xhtml.startElement("div", "class", "annotationContents");
                                    this.xhtml.characters(contents);
                                    this.xhtml.endElement("div");
                                }

                                this.xhtml.endElement("div");
                            }
                        }
                    }
                }

                if(this.config.getOcrStrategy().equals(SCSParserPDFConfig.OCR_STRATEGY.OCR_AND_TEXT_EXTRACTION)) {
                    this.doOCROnCurrentPage();
                }

                PDPageAdditionalActions pageActions = page.getActions();
                if(pageActions != null) {
                    this.handleDestinationOrAction(pageActions.getC(),ActionTrigger.PAGE_CLOSE);
                    this.handleDestinationOrAction(pageActions.getO(),ActionTrigger.PAGE_OPEN);
                }

                this.xhtml.endElement("div");
            } catch (TikaException | SAXException var19) {
                throw new IOExceptionWithCause("Unable to end a page", var19);
            } catch (IOException var20) {
                this.exceptions.add(var20);
            } finally {
                ++this.pageIndex;
            }

        }

        private void handleWidget(PDAnnotationWidget widget) throws TikaException, SAXException, IOException {
            if(widget != null) {
                this.handleDestinationOrAction(widget.getAction(), ActionTrigger.ANNOTATION_WIDGET);
                PDAnnotationAdditionalActions annotationActions = widget.getActions();
                if(annotationActions != null) {
                    this.handleDestinationOrAction(annotationActions.getBl(),ActionTrigger.ANNOTATION_LOSE_INPUT_FOCUS);
                    this.handleDestinationOrAction(annotationActions.getD(),ActionTrigger.ANNOTATION_MOUSE_CLICK);
                    this.handleDestinationOrAction(annotationActions.getE(),ActionTrigger.ANNOTATION_CURSOR_ENTERS);
                    this.handleDestinationOrAction(annotationActions.getFo(),ActionTrigger.ANNOTATION_RECEIVES_FOCUS);
                    this.handleDestinationOrAction(annotationActions.getPC(),ActionTrigger.ANNOTATION_PAGE_CLOSED);
                    this.handleDestinationOrAction(annotationActions.getPI(),ActionTrigger.ANNOTATION_PAGE_NO_LONGER_VISIBLE);
                    this.handleDestinationOrAction(annotationActions.getPO(),ActionTrigger.ANNOTATION_PAGE_OPENED);
                    this.handleDestinationOrAction(annotationActions.getPV(),ActionTrigger.ANNOTATION_PAGE_VISIBLE);
                    this.handleDestinationOrAction(annotationActions.getU(),ActionTrigger.ANNOTATION_MOUSE_RELEASED);
                    this.handleDestinationOrAction(annotationActions.getX(),ActionTrigger.ANNOTATION_CURSOR_EXIT);
                }

            }
        }

        protected void startDocument(PDDocument pdf) throws IOException {
            try {
                this.xhtml.startDocument();

                try {
                    this.handleDestinationOrAction(pdf.getDocumentCatalog().getOpenAction(), SCSAbstractPDF2XHTML.ActionTrigger.DOCUMENT_OPEN);
                } catch (IOException var3) {
                    ;
                }

            } catch (SAXException | TikaException var4) {
                throw new IOExceptionWithCause("Unable to start a document", var4);
            }
        }

        private void handleDestinationOrAction(PDDestinationOrAction action, SCSAbstractPDF2XHTML.ActionTrigger actionTrigger) throws IOException, SAXException, TikaException {
            if(action != null && this.config.getExtractActions()) {
                AttributesImpl attributes = new AttributesImpl();
                String actionOrDestString = action instanceof PDAction ?"action":"destination";
                addNonNullAttribute("class", actionOrDestString, attributes);
                addNonNullAttribute("type", action.getClass().getSimpleName(), attributes);
                addNonNullAttribute("trigger", actionTrigger.name(), attributes);
                if(action instanceof PDActionImportData) {
                    this.processDoc("", ((PDActionImportData)action).getFile(), attributes);
                } else if(action instanceof PDActionLaunch) {
                    PDActionLaunch pdActionLaunch = (PDActionLaunch)action;
                    addNonNullAttribute("id", pdActionLaunch.getF(), attributes);
                    addNonNullAttribute("defaultDirectory", pdActionLaunch.getD(), attributes);
                    addNonNullAttribute("operation", pdActionLaunch.getO(), attributes);
                    addNonNullAttribute("parameters", pdActionLaunch.getP(), attributes);
                    this.processDoc(pdActionLaunch.getF(), pdActionLaunch.getFile(), attributes);
                } else if(action instanceof PDActionRemoteGoTo) {
                    PDActionRemoteGoTo remoteGoTo = (PDActionRemoteGoTo)action;
                    this.processDoc("", remoteGoTo.getFile(), attributes);
                } else if(action instanceof PDActionJavaScript) {
                    PDActionJavaScript jsAction = (PDActionJavaScript)action;
                    Metadata m = new Metadata();
                    m.set("Content-Type", "application/javascript");
                    m.set("Content-Encoding", StandardCharsets.UTF_8.toString());
                    m.set(PDF.ACTION_TRIGGER, actionTrigger.toString());
                    m.set(TikaCoreProperties.EMBEDDED_RESOURCE_TYPE, TikaCoreProperties.EmbeddedResourceType.MACRO.name());
                    String js = jsAction.getAction();
                    js = js == null?"":js;
                    if(this.embeddedDocumentExtractor.shouldParseEmbedded(m)) {
                        InputStream is = TikaInputStream.get(js.getBytes(StandardCharsets.UTF_8));
                        Throwable var9 = null;

                        try {
                            this.embeddedDocumentExtractor.parseEmbedded(is, this.xhtml, m, false);
                        } catch (Throwable var18) {
                            var9 = var18;
                            throw var18;
                        } finally {
                            if(is != null) {
                                if(var9 != null) {
                                    try {
                                        is.close();
                                    } catch (Throwable var17) {
                                        var9.addSuppressed(var17);
                                    }
                                } else {
                                    is.close();
                                }
                            }

                        }
                    }

                    addNonNullAttribute("class", "javascript", attributes);
                    addNonNullAttribute("type", jsAction.getType(), attributes);
                    addNonNullAttribute("subtype", jsAction.getSubType(), attributes);
                    this.xhtml.startElement("div", attributes);
                    this.xhtml.endElement("div");
                } else {
                    this.xhtml.startElement("div", attributes);
                    this.xhtml.endElement("div");
                }

            }
        }

        private static void addNonNullAttribute(String name, String value, AttributesImpl attributes) {
            if(name != null && value != null) {
                attributes.addAttribute("", name, name, "CDATA", value);
            }
        }

        protected void endDocument(PDDocument pdf) throws IOException {
            try {
                if(this.config.getExtractBookmarksText()) {
                    this.extractBookmarkText();
                }

                try {
                    this.extractEmbeddedDocuments(pdf);
                } catch (IOException var4) {
                    this.handleCatchableIOE(var4);
                }

                if(this.config.getExtractAcroFormContent()) {
                    try {
                        this.extractAcroForm(pdf);
                    } catch (IOException var3) {
                        this.handleCatchableIOE(var3);
                    }
                }

                PDDocumentCatalogAdditionalActions additionalActions = pdf.getDocumentCatalog().getActions();
                this.handleDestinationOrAction(additionalActions.getDP(),ActionTrigger.AFTER_DOCUMENT_PRINT);
                this.handleDestinationOrAction(additionalActions.getDS(),ActionTrigger.AFTER_DOCUMENT_SAVE);
                this.handleDestinationOrAction(additionalActions.getWC(),ActionTrigger.BEFORE_DOCUMENT_CLOSE);
                this.handleDestinationOrAction(additionalActions.getWP(),ActionTrigger.BEFORE_DOCUMENT_PRINT);
                this.handleDestinationOrAction(additionalActions.getWS(),ActionTrigger.BEFORE_DOCUMENT_SAVE);
                this.xhtml.endDocument();
            } catch (TikaException var5) {
                throw new IOExceptionWithCause("Unable to end a document", var5);
            } catch (SAXException var6) {
                throw new IOExceptionWithCause("Unable to end a document", var6);
            }
        }

        void extractBookmarkText() throws SAXException, IOException, TikaException {
            PDDocumentOutline outline = this.document.getDocumentCatalog().getDocumentOutline();
            if(outline != null) {
                this.extractBookmarkText(outline);
            }

        }

        void extractBookmarkText(PDOutlineNode bookmark) throws SAXException, IOException, TikaException {
            PDOutlineItem current = bookmark.getFirstChild();
            if(current != null) {
                this.xhtml.startElement("ul");

                while(current != null) {
                    this.xhtml.startElement("li");
                    this.xhtml.characters(current.getTitle());
                    this.xhtml.endElement("li");
                    this.handleDestinationOrAction(current.getAction(),ActionTrigger.BOOKMARK);
                    this.extractBookmarkText(current);
                    current = current.getNextSibling();
                }

                this.xhtml.endElement("ul");
            }

        }

        void extractAcroForm(PDDocument pdf) throws IOException, SAXException, TikaException {
            PDDocumentCatalog catalog = pdf.getDocumentCatalog();
            if(catalog != null) {
                PDAcroForm form = catalog.getAcroForm();
                if(form != null) {
                    PDXFAResource pdxfa = form.getXFA();
                    if(pdxfa != null) {
                        SCSExtractorXFA xfaExtractor = new SCSExtractorXFA();
                        BufferedInputStream is = null;

                        try {
                            is = new BufferedInputStream(new ByteArrayInputStream(pdxfa.getBytes()));
                        } catch (IOException var12) {
                            EmbeddedDocumentUtil.recordEmbeddedStreamException(var12, this.metadata);
                        }

                        if(is != null) {
                            label109: {
                                try {
                                    xfaExtractor.extract(is, this.xhtml, this.metadata, this.context);
                                } catch (XMLStreamException var13) {
                                    EmbeddedDocumentUtil.recordException(var13, this.metadata);
                                    break label109;
                                } finally {
                                    IOUtils.closeQuietly(is);
                                }

                                return;
                            }
                        }
                    }

                    List fields = form.getFields();
                    if(fields != null) {
                        ListIterator itr = fields.listIterator();
                        if(itr != null) {
                            this.xhtml.startElement("div", "class", "acroform");
                            this.xhtml.startElement("ol");

                            while(itr.hasNext()) {
                                Object obj = itr.next();
                                if(obj != null && obj instanceof PDField) {
                                    this.processAcroField((PDField)obj, 0);
                                }
                            }

                            this.xhtml.endElement("ol");
                            this.xhtml.endElement("div");
                        }
                    }
                }
            }
        }

        private void processAcroField(PDField field, int currentRecursiveDepth) throws SAXException, IOException, TikaException {
            if(currentRecursiveDepth < 10) {
                PDFormFieldAdditionalActions pdFormFieldAdditionalActions = field.getActions();
                if(pdFormFieldAdditionalActions != null) {
                    this.handleDestinationOrAction(pdFormFieldAdditionalActions.getC(),ActionTrigger.FORM_FIELD_RECALCULATE);
                    this.handleDestinationOrAction(pdFormFieldAdditionalActions.getF(),ActionTrigger.FORM_FIELD_FORMATTED);
                    this.handleDestinationOrAction(pdFormFieldAdditionalActions.getK(),ActionTrigger.FORM_FIELD_KEYSTROKE);
                    this.handleDestinationOrAction(pdFormFieldAdditionalActions.getV(),ActionTrigger.FORM_FIELD_VALUE_CHANGE);
                }

                if(field.getWidgets() != null) {
                    Iterator var4 = field.getWidgets().iterator();

                    while(var4.hasNext()) {
                        PDAnnotationWidget widget = (PDAnnotationWidget)var4.next();
                        this.handleWidget(widget);
                    }
                }

                this.addFieldString(field);
                if(field instanceof PDNonTerminalField) {
                    int r = currentRecursiveDepth + 1;
                    this.xhtml.startElement("ol");
                    Iterator var8 = ((PDNonTerminalField)field).getChildren().iterator();

                    while(var8.hasNext()) {
                        PDField child = (PDField)var8.next();
                        this.processAcroField(child, r);
                    }

                    this.xhtml.endElement("ol");
                }

            }
        }

        private void addFieldString(PDField field) throws SAXException {
            String partName = field.getPartialName();
            String altName = field.getAlternateFieldName();
            StringBuilder sb = new StringBuilder();
            AttributesImpl attrs = new AttributesImpl();
            if(partName != null) {
                sb.append(partName).append(": ");
            }

            if(altName != null) {
                attrs.addAttribute("", "altName", "altName", "CDATA", altName);
            }

            if(field instanceof PDSignatureField) {
                this.handleSignature(attrs, (PDSignatureField)field);
            } else {
                String value = field.getValueAsString();
                if(value != null && !value.equals("null")) {
                    sb.append(value);
                }

                if(attrs.getLength() > 0 || sb.length() > 0) {
                    this.xhtml.startElement("li", attrs);
                    this.xhtml.characters(sb.toString());
                    this.xhtml.endElement("li");
                }

            }
        }

        private void handleSignature(AttributesImpl parentAttributes, PDSignatureField sigField) throws SAXException {
            PDSignature sig = sigField.getSignature();
            if(sig != null) {
                Map<String, String> vals = new TreeMap();
                vals.put("name", sig.getName());
                vals.put("contactInfo", sig.getContactInfo());
                vals.put("location", sig.getLocation());
                vals.put("reason", sig.getReason());
                Calendar cal = sig.getSignDate();
                if(cal != null) {
                    this.dateFormat.setTimeZone(cal.getTimeZone());
                    vals.put("date", this.dateFormat.format(cal.getTime()));
                }

                int nonNull = 0;
                Iterator var7 = vals.keySet().iterator();

                while(var7.hasNext()) {
                    String val = (String)var7.next();
                    if(val != null && !val.equals("")) {
                        ++nonNull;
                    }
                }

                if(nonNull > 0) {
                    this.xhtml.startElement("li", parentAttributes);
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "type", "type", "CDATA", "signaturedata");
                    this.xhtml.startElement("ol", attrs);
                    Iterator var11 = vals.entrySet().iterator();

                    while(var11.hasNext()) {
                        Map.Entry<String, String> e = (Map.Entry)var11.next();
                        if(e.getValue() != null && !((String)e.getValue()).equals("")) {
                            attrs = new AttributesImpl();
                            attrs.addAttribute("", "signdata", "signdata", "CDATA", (String)e.getKey());
                            this.xhtml.startElement("li", attrs);
                            this.xhtml.characters((String)e.getValue());
                            this.xhtml.endElement("li");
                        }
                    }

                    this.xhtml.endElement("ol");
                    this.xhtml.endElement("li");
                }

            }
        }

        private static PDActionURI getActionURI(PDAnnotation annot) {
            try {
                Method actionMethod = annot.getClass().getDeclaredMethod("getAction", new Class[0]);
                if(actionMethod.getReturnType().equals(PDAction.class)) {
                    PDAction action = (PDAction)actionMethod.invoke(annot, new Object[0]);
                    if(action instanceof PDActionURI) {
                        return (PDActionURI)action;
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var3) {
                ;
            }

            return null;
        }

        static enum ActionTrigger {
            AFTER_DOCUMENT_PRINT,
            AFTER_DOCUMENT_SAVE,
            ANNOTATION_CURSOR_ENTERS,
            ANNOTATION_CURSOR_EXIT,
            ANNOTATION_LOSE_INPUT_FOCUS,
            ANNOTATION_MOUSE_CLICK,
            ANNOTATION_MOUSE_RELEASED,
            ANNOTATION_PAGE_CLOSED,
            ANNOTATION_PAGE_NO_LONGER_VISIBLE,
            ANNOTATION_PAGE_OPENED,
            ANNOTATION_PAGE_VISIBLE,
            ANNOTATION_RECEIVES_FOCUS,
            ANNOTATION_WIDGET,
            BEFORE_DOCUMENT_CLOSE,
            BEFORE_DOCUMENT_PRINT,
            BEFORE_DOCUMENT_SAVE,
            DOCUMENT_OPEN,
            FORM_FIELD,
            FORM_FIELD_FORMATTED,
            FORM_FIELD_KEYSTROKE,
            FORM_FIELD_RECALCULATE,
            FORM_FIELD_VALUE_CHANGE,
            PAGE_CLOSE,
            PAGE_OPEN,
            BOOKMARK;

            private ActionTrigger() {
            }
        }
    }

