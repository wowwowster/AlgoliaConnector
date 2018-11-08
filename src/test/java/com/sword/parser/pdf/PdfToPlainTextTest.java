package com.sword.parser.pdf;

import com.sword.ParsingHandler;
import com.sword.gsa.spis.scs.algolia.AlgoliaTest;
import org.apache.tika.metadata.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PdfToPlainTextTest extends AlgoliaTest {

    private static final String PDF_FILENAME_TEST_FIRST = "Code-de-deontologie.pdf";
    private static final String PDF_FILENAME_TEST_SECOND = "le-journal-ordre-pharmaciens-35.pdf";
    private static final String ONE_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologie.xml";
    private static final String BBBBBONE_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologieB.xml";
    private static final String SKIP_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologie_skipbom.xml";
    private static final String ONE_TEST_GENERATED_JSON_FILENAME = "output/xml/Code-de-deontologie.json";
    private static int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static final String STD_ENCODING = "UTF-8";
    private static final int MAXIMUM_TEXT_CHUNK_SIZE = 143;

    @Test
    public void tikaPdfConversion() {
        try {
            clearTestingData();
            handlePdfDoc(PDF_FILENAME_TEST_SECOND);
            System.out.println("tout va bien ");
        } catch (Exception e) {
            System.out.println("handlePage = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
        }

    }

    private void handlePdfDoc(String pdfFilename) {
        try {
            Metadata metadata = new Metadata();


            PrintWriter fileWriter = new PrintWriter("output/txt/resultatsTika " + pdfFilename + ".txt");
            System.out.println("Metadata of the PDF:");
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                System.out.println(name + " : " + metadata.get(name));
            }

            List<String> chunks = ParsingHandler.parseToPlainTextChunks(pdfFilename);
            for (String chunk : chunks) {
                fileWriter.println(chunk);
            }



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

