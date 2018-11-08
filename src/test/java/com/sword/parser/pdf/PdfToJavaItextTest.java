package com.sword.parser.pdf;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TaggedPdfReaderTool;
import com.sword.gsa.spis.scs.service.dto.DocumentDTO;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;
import com.sword.gsa.spis.scs.algolia.AlgoliaTest;
import com.sword.gsa.spis.scs.utils.algolia.AlgoliaUtils;
import com.sword.gsa.spis.scs.utils.string.TextSplitter;
import com.sword.xml.UnicodeBOMInputStream;
import org.apache.commons.io.IOUtils;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;


import java.io.*;
import java.io.FileOutputStream;
import java.io.IOException;


import org.json.JSONObject;
import org.json.XML;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import static com.itextpdf.text.pdf.parser.PdfContentReaderTool.listContentStream;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PdfToJavaItextTest extends AlgoliaTest {

    private static final String PDF_FILENAME_TEST_FIRST = "Code-de-deontologie.pdf";
    private static final String PDF_FILENAME_TEST_SECOND =  "le-journal-ordre-pharmaciens-35.pdf";
    private static final String ONE_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologie.xml";
    private static final String BBBBBONE_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologieB.xml";
    private static final String SKIP_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologie_skipbom.xml";
    private static final String ONE_TEST_GENERATED_JSON_FILENAME = "output/xml/Code-de-deontologie.json";
    private static int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static final String STD_ENCODING = "UTF-8";

    @Test
    public void pdfToJsonConversion() {
        TaggedPdfReaderTool readertool = new TaggedPdfReaderTool();

        try {
            PdfReader reader = new PdfReader(PDF_FILENAME_TEST_FIRST);
            readertool.convertToXml(reader, new FileOutputStream(ONE_TEST_GENERATED_XML_FILENAME), "UTF-8");
            reader.close();
            FileInputStream fis = new FileInputStream(ONE_TEST_GENERATED_XML_FILENAME);
            UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
            ubis.skipBOM();
            IOUtils.copy(ubis, new FileOutputStream(SKIP_TEST_GENERATED_XML_FILENAME));

        } catch (IOException e) {
            System.out.println("main - ioexception=" + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("tout va bien ");
    }

    @Test
    public void convertPdfFiles() {
        try { clearTestingData();
         //   extract(PDF_FILENAME_TEST_FIRST);
            handlePdfDoc(PDF_FILENAME_TEST_SECOND);
            System.out.println("tout va bien ");
        } catch (Exception e) {
            System.out.println("extract = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
        }

    }


    /** 1 page, bloc de 100 , ca marche  */
    private void handlePdfDoc(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource(filename);
            PrintWriter writer = new PrintWriter(System.out);
            List<TextBlockDTO>  listTextBlockDTO = new ArrayList<>();

            /* spécifique Itext */
            PdfReader reader = new PdfReader(filename);

            // listContentStream(resource.getFile(), writer);
            PrintWriter fileWriter = new PrintWriter ("output/txt/resultatsItext " +filename +".txt");
            //listContentStream(resource.getFile(), fileWriter);

            // décomposition

            int maxPageNum = reader.getNumberOfPages() ;
            /* pas performant
            DocumentDTO documentDTO = new  DocumentDTO().setName(filename).setTotalPages(maxPageNum ).setIndexationDate(LocalDateTime.now());
            TaskIndexing task = indexDocument.addObject(documentDTO);
            waitForCompletion(task);  */

            for(int pageNum = 1; pageNum <= maxPageNum; ++pageNum) {
                // PdfDictionary pageDictionary = reader.getPageN(pageNum);
              /* RandomAccessFileOrArray f = reader.getSafeFile();
                byte[] contentBytes = reader.getPageContent(pageNum, f);
                f.close();
                out.flush();
                ByteArrayInputStream is = new ByteArrayInputStream(contentBytes);

                int ch;
                while((ch = is.read()) != -1) {
                    out.print((char)ch);
                }

                out.flush();
                out.println("- - - - - Text Extraction - - - - - -"); */
                String extractedText = PdfTextExtractor.getTextFromPage(reader, pageNum, new LocationTextExtractionStrategy());

                /* méthode d'Itext correspondante  = listContentStreamForPage(reader, pageNum, writer); */
                // documentDTO.addPage(new PageDTO().setNumero(pageNum).setExtractedText(extractedText));

               /* pas performant
                documentDTO.addPage(new PageDTO().setNumero(pageNum));

                //waitForCompletion(indexDocument.partialUpdateObject(task.getObjectID(), documentDTO));
 */

                List<String> previews = TextSplitter.splitString(extractedText.replaceAll("[\r\n]+", " "), 200);
                LocalDateTime now =  LocalDateTime.now();
                DocumentDTO newDocument = new DocumentDTO().setName(PDF_FILENAME_TEST_SECOND).setIndexationDate(now);
                for (String preview : previews) {
                    listTextBlockDTO.add(new TextBlockDTO().setDocument(newDocument).setInsertNumber(pageNum).setContent(preview).setObjectID(UUID.randomUUID().toString()));
                    /*  pas performant
                     documentDTO.getPages().get(pageNum-1).addTextBlock(preview);
                    waitForCompletion(indexDocument.partialUpdateObject(task.getObjectID(), documentDTO));
                    Thread.sleep(30000); */
                }

                // Splitter.fixedLength(240).omitEmptyStrings().splitToList(extractedText);

            }

            AlgoliaUtils.waitForCompletion(indexTextBlock.addObjects(listTextBlockDTO), client);
        } catch (Exception e) {
            System.out.println("extract = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
            fail();
        }


    }


    private static String processXML2JSON(String xmlDoc) throws JSONException {
        String json = null;
        try {
            String TEST_XML_STRING = new String(Files.readAllBytes(Paths.get(xmlDoc)));
            try {
                JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
                String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
                System.out.println(jsonPrettyPrintString);
            } catch (JSONException je) {
                System.out.println(je.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }


   /* String.prototype.summarize = function (maxLength) {
        let self = this
        if (self.length > 0) {
            if (self.length > maxLength) {
                let lastWordPos = maxLength
                while (self.charAt(lastWordPos) != ' ') {
                    lastWordPos--
                }
                return self.substring(0, lastWordPos) + '...'
            } else {
                return self
            }
        } else return ''
    }  */

}
