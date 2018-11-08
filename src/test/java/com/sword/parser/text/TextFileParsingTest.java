package com.sword.parser.text;

import com.sword.ParsingHandler;
import com.sword.gsa.spis.scs.algolia.AlgoliaTest;
import com.sword.gsa.spis.scs.service.dto.DocumentDTO;
import com.sword.gsa.spis.scs.service.dto.ParagraphDTO;
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


@RunWith(SpringRunner.class)
@SpringBootTest
public class TextFileParsingTest extends AlgoliaTest {

    private static final String TEXT_PLAIN_FILENAME = "asup.txt";

    @Test
    public void tikaTxtConversion() {
        try {
            clearTestingData();
            handleTxtDoc(TEXT_PLAIN_FILENAME);
            System.out.println("tout va bien ");
        } catch (Exception e) {
            System.out.println("handlePage = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
        }

    }

    private void handleTxtDoc(String txtFilename) {
        try {
            ClassPathResource resource = new ClassPathResource(txtFilename);
            List<TextBlockDTO> listTextBlockDTO = new ArrayList<>();

            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            InputStream inputStream = new FileInputStream(resource.getFile());
            ParseContext pcontext = new ParseContext();
            LocalDateTime lastModified = LocalDateTime.now();

            PrintWriter fileWriter = new PrintWriter("output/txt/resultatsTika " + txtFilename + ".doc");
            PrintWriter htmlFileWriter = new PrintWriter("output/txt/resultatsTika " + txtFilename + ".html");

            //String extractedTextHTML = SCSContentHandler.parseInputStreamToTextBlocks(txtFilename);
            String extractedTextHTML = ParsingHandler.parseHTMLInputStreamToString(inputStream, metadata);

            //String extractedText = handler.toString();
            System.out.println("Metadata of the TXT:");
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                System.out.println(name + " : " + metadata.get(name));
            }

            htmlFileWriter.print(extractedTextHTML);
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
                        fileWriter.println(textBlockText);
                        fileWriter.println("");
                        lastLinkText = textBlockText;
                        DocumentDTO newDocument = new DocumentDTO().setName(TEXT_PLAIN_FILENAME).setIndexationDate(lastModified);

                        listTextBlockDTO.add(new TextBlockDTO().setDocument(newDocument).setInsertNumber(++insertNumber).setPageNumber(indexPage).setContent(textBlockText).setObjectID(UUID.randomUUID().toString()));
                    }
                }
            }

            waitForCompletion(indexTextBlock.addObjects(listTextBlockDTO), client);
            fileWriter.close();
            htmlFileWriter.close();
        } catch (Exception e) {
            System.out.println("extract = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
            fail();
        }

    }

    public static LocalDateTime tryToParse(String dateString) {
        LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        return dateTime;
    }


}
