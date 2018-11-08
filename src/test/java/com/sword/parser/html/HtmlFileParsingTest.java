package com.sword.parser.html;

import com.sword.ParsingHandler;
import com.sword.gsa.spis.scs.algolia.AlgoliaTest;
import com.sword.gsa.spis.scs.service.dto.DocumentDTO;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sword.gsa.spis.scs.utils.algolia.AlgoliaUtils.waitForCompletion;
import static org.junit.Assert.fail;


/* VM options
    -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider

 */


@RunWith(SpringRunner.class) @SpringBootTest public class HtmlFileParsingTest extends AlgoliaTest {

    private static final String HTML_PLAIN_FILENAME = "testHTML.html";

    @Test public void tikaHtmlConversion() {
        try {
            clearTestingData();
            parseHTMLDocument(HTML_PLAIN_FILENAME);
            System.out.println("tout va bien ");
        } catch (Exception e) {
            System.out.println("handlePage = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
        }

    }

    private void parseHTMLDocument(String htmlFilename) {
        try {
            ClassPathResource resource = new ClassPathResource(htmlFilename);
            List<TextBlockDTO> listTextBlockDTO = new ArrayList<>();

            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            InputStream inputStream = new FileInputStream(resource.getFile());
            ParseContext pcontext = new ParseContext();
            LocalDateTime lastModified = LocalDateTime.now();

            PrintWriter txtFileWriter =
                new PrintWriter("output/html/resultatsTika " + htmlFilename + ".txt");

            String extractedTextHTML =
                ParsingHandler.parseHTMLInputStreamToString(inputStream, metadata);

            //String extractedText = handler.toString();
            System.out.println("Metadata of the HTML:");
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                System.out.println(name + " : " + metadata.get(name));
            }

            txtFileWriter.println(extractedTextHTML);
            Document doc = Jsoup.parse(extractedTextHTML);
            Element body = doc.body();

            Elements pages = body.children();
            int insertNumber = 0;
            for (int indexPage = 1; indexPage < pages.size(); indexPage++) {

                Elements textBlocks = pages.get(indexPage - 1).getElementsByTag("p");
                String lastLinkText = "";
                for (Element textBlock : textBlocks) {
                    String textBlockText = textBlock.text().replaceAll("- ", "");
                    if (textBlockText != null && textBlockText.length() > 0  //14
                        && !textBlockText.equalsIgnoreCase(lastLinkText)) {
                        txtFileWriter.println(textBlockText);
                        txtFileWriter.println("");
                        lastLinkText = textBlockText;

                        DocumentDTO newDocument = new DocumentDTO().setName(HTML_PLAIN_FILENAME)
                            .setIndexationDate(lastModified);

                        listTextBlockDTO.add(new TextBlockDTO().setDocument(newDocument)
                            .setInsertNumber(++insertNumber).setPageNumber(indexPage)
                            .setContent(textBlockText).setObjectID(UUID.randomUUID().toString()));

                    }
                }
            }

            waitForCompletion(indexTextBlock.addObjects(listTextBlockDTO), client);
            txtFileWriter.close();

        } catch (Exception e) {
            System.out.println("extract = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
            fail();
        }
        System.out.println("tout va bien");
    }

    public static LocalDateTime tryToParse(String dateString) {
        LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        return dateTime;
    }


}
