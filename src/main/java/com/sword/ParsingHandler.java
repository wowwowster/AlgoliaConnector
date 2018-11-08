package com.sword;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.sword.gsa.spis.scs.push.connector.threading.IndexingTask;
import com.sword.gsa.spis.scs.push.throwables.DoNotIndex;
import org.apache.log4j.Logger;
import com.sword.gsa.spis.scs.CoreConstants;
import com.sword.gsa.spis.scs.extracting.AParsingHandler;
import com.sword.gsa.spis.scs.service.dto.DocumentDTO;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;
import com.sword.gsa.spis.scs.service.dto.ThematiqueDTO;
import com.sword.gsa.spis.scs.service.dto.TypeDocumentDTO;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.ToXMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class ParsingHandler extends AParsingHandler {

    protected static final Logger logger = Logger.getLogger(ParsingHandler.class);

    public ParsingHandler() {
        super();
    }

    @Override
    public List<TextBlockDTO> parseInputStreamToTextBlocks(InputStream inputStream, Metadata tikaMetadata)
        throws IOException, SAXException, TikaException, DoNotIndex {

        List<String> paragraphs = null;
        String ezObjectFileType = "";
        String ezObjectType = "";
        String ezObjectName = "";
        String ezObjectText = "";
        String ezObjectId = "";
        String ezSource = "";
        String docURL = "";
        String theme_level0 = "";
        String theme_level1 = "";
        String theme_level2 = "";
        String subDocumentType_cible = "";
        String subDocumentType_nature = "";
        String typeDocumentLvl0 = "";
        List<TextBlockDTO> listTextBlockDTO = new ArrayList<>();

        ezObjectFileType = Optional.ofNullable(tikaMetadata.get(IndexingTask.SYS_PARAMETER_MIME)).orElse(""); // exemple : "text/html"
        ezObjectType = Optional.ofNullable(tikaMetadata.get("Type")).orElse(""); // exemple : "speech"
        ezObjectName =
            tikaMetadata.get("name") != null ? tikaMetadata.get("name") : tikaMetadata.get("short_title") != null ? tikaMetadata.get("short_title") : "";
        ezObjectText = tikaMetadata.get("text") != null ? tikaMetadata.get("text") : tikaMetadata.get("body") != null ? tikaMetadata.get("body") : "";
        ezObjectId = Optional.ofNullable(tikaMetadata.get("SwordID")).orElse("");
        ezSource = Optional.ofNullable(StringUtils.capitalize(tikaMetadata.get("SwordSource"))).orElse("");
        docURL = Optional.ofNullable(tikaMetadata.get("SwordURL")).orElse("");
        theme_level0 = Optional.ofNullable(tikaMetadata.get("themes00_name")).orElse("");
        theme_level1 = Optional.ofNullable(tikaMetadata.get("themes01_name")).orElse("");
        theme_level2 = Optional.ofNullable(tikaMetadata.get("themes02_name")).orElse("");
        subDocumentType_cible = Optional.ofNullable(tikaMetadata.get("cible_name")).orElse("");
        subDocumentType_nature = Optional.ofNullable(tikaMetadata.get("nature_name")).orElse("");

        logger.debug("Filename=" + ezObjectName + "fileType=" + ezObjectFileType);
        if (Constants.EZPUBLISH_DOCUMENT_TYPE_TO_PROCESS.get(ezObjectType) != null) {
            typeDocumentLvl0 = Constants.EZPUBLISH_DOCUMENT_TYPE_TO_PROCESS.get(ezObjectType);
            LocalDateTime indexationDateTime = LocalDateTime.now();
            TypeDocumentDTO typeDocument = new TypeDocumentDTO();
            ThematiqueDTO thematique = new ThematiqueDTO();
            if (!StringUtils.isEmpty(theme_level0)) {
                thematique = thematique.setLvl0(theme_level0);
            }
            if (!StringUtils.isEmpty(theme_level1)) {
                thematique.addLvl1(theme_level0 + Constants.METADATA_SEPARATOR + theme_level1);
            }
            if (!StringUtils.isEmpty(theme_level2)) {
                thematique.addLvl2(theme_level0 + Constants.METADATA_SEPARATOR + theme_level1 + Constants.METADATA_SEPARATOR + theme_level2);
            }
            if (!StringUtils.isEmpty(typeDocumentLvl0)) {
                typeDocument = typeDocument.setLvl0(typeDocumentLvl0);
                if (!StringUtils.isEmpty(subDocumentType_cible)) {
                    typeDocument.addLvl1(typeDocumentLvl0 + Constants.METADATA_SEPARATOR + "Cible");
                    typeDocument.addLvl2(typeDocumentLvl0 + Constants.METADATA_SEPARATOR + "Cible" + Constants.METADATA_SEPARATOR + subDocumentType_cible);
                }
                if (!StringUtils.isEmpty(subDocumentType_nature)) {
                    typeDocument.addLvl1(typeDocumentLvl0 + Constants.METADATA_SEPARATOR + "Nature");
                    typeDocument.addLvl2(typeDocumentLvl0 + Constants.METADATA_SEPARATOR + "Nature" + Constants.METADATA_SEPARATOR + subDocumentType_nature);
                }
            }
            DocumentDTO newDocument = new DocumentDTO().setId(ezObjectId).setName(ezObjectName).setMimeType(ezObjectFileType).setUrl(docURL)
                .setIndexationDate(indexationDateTime).setSite(ezSource).setTypeDocument(typeDocument).setThematique(thematique);
            int insertNumber = 0;
            if (ezObjectFileType.startsWith(CoreConstants.HTML_MIME)) {
                if (!ezObjectText.isEmpty()) {
                    paragraphs = parseXML(ezObjectText);
                    for (String paragraph : paragraphs) {
                        if (paragraph != null && paragraph.trim().length() > 0) {
                            listTextBlockDTO.add(new TextBlockDTO().setDocument(newDocument).setInsertNumber(++insertNumber).setPageNumber(0).setContent(paragraph)
                                .setObjectID(UUID.randomUUID().toString()));
                        }
                    }
                }
            } else {
                ContentHandler handler = new ToXMLContentHandler();
                parser.parse(inputStream, handler, tikaMetadata);
                org.jsoup.nodes.Document doc = Jsoup.parse(handler.toString());
                Element body = doc.body();
                Elements pages = body.children();
                for (int indexPage = 0; indexPage < pages.size(); indexPage++) {
                    Elements htmlParagraphs = pages.get(indexPage).getElementsByTag("p");
                    String lastLinkText = "";
                    for (Element paragraph : htmlParagraphs) {
                        String paragraphText = paragraph.text().replaceAll("- ", "");
                        if (paragraphText != null && paragraphText.length() > 0
                            && !paragraphText.equalsIgnoreCase(lastLinkText)) {
                            lastLinkText = paragraphText;
                            listTextBlockDTO.add(new TextBlockDTO().setDocument(newDocument).setInsertNumber(++insertNumber).setPageNumber(indexPage)
                                .setContent(paragraphText).setObjectID(UUID.randomUUID().toString()));

                        }
                    }
                }
            }
        } else
            throw new DoNotIndex("EzObject unknown type - name = " + ezObjectName);
        if (listTextBlockDTO == null  || (listTextBlockDTO != null && listTextBlockDTO.isEmpty())) {
            throw new DoNotIndex("EzObject without indexable content - name = " + ezObjectName);
        }
        return listTextBlockDTO;
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub

    }

}
