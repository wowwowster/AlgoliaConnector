package com.sword.parser.pdf;

import com.sword.ParsingHandler;
import com.sword.gsa.spis.scs.algolia.AlgoliaTest;
import com.sword.gsa.spis.scs.extracting.AParsingHandler;
import com.sword.gsa.spis.scs.push.throwables.DoNotIndex;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.tika.metadata.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.sword.gsa.spis.scs.utils.algolia.AlgoliaUtils.waitForCompletion;
import static org.junit.Assert.fail;
import com.sword.ParsingHandler;


/* VM options
    -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider

 */


@RunWith(SpringRunner.class)
@SpringBootTest
public class PdfToJavaTikaTest extends AlgoliaTest {

    private static final String PDF_FILENAME_TEST_FIRST = "Code-de-deontologie.pdf";
    private static final String PDF_FILENAME_TEST_SECOND = "RETRAITS DE LOTS-INFO (27.04.2016).pdf";
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
            this.getClass().getResourceAsStream("PDFParser.properties");
            clearTestingData();
            handlePdfDoc(PDF_FILENAME_TEST_FIRST);
            handlePdfDoc(PDF_FILENAME_TEST_SECOND);

            System.out.println("tout va bien ");
        } catch (Exception e) {
            System.out.println("handlePage = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
        }

    }

    private void handlePdfDoc(String pdfFilename) {
        List<TextBlockDTO> listTextBlockDTO = null;
        try {
            ClassPathResource resource = new ClassPathResource(pdfFilename);
            Metadata metadata = new Metadata();
            InputStream inputStream = new FileInputStream(resource.getFile());
            PrintWriter htmlFileWriter = new PrintWriter("output/txt/resultatsTika " + pdfFilename + ".html");
            metadata.set("SwordMIME", "application/pdf");
            metadata.set("Type", "file");
            metadata.set("name", "Définitions des thèmes");
            metadata.set("text", "");
            metadata.set("SwordID", "file|81290");
            metadata.set("nonbinary_url", "http://www.eqo.fr/media/files/definitions-des-themes");
            try {
                AParsingHandler parsingHandler = new ParsingHandler();
               listTextBlockDTO =
                    parsingHandler.parseInputStreamToTextBlocks(inputStream, metadata);
            } catch (DoNotIndex doNotIndex) {
                doNotIndex.printStackTrace();
            }
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("Metadata of the PDF:");
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                System.out.println(name + " : " + metadata.get(name));
            }
            htmlFileWriter.print(listTextBlockDTO);

            // TODO contrôler la valeur de last modified
            waitForCompletion(indexTextBlock.addObjects(listTextBlockDTO), client);

            /* Découpage en image  */
            PDDocument document = PDDocument.load(resource.getFile());
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int numberOfPages = document.getNumberOfPages() < 5 ? document.getNumberOfPages() : 5;

            for (int page = 0; page < numberOfPages; ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                // suffix in pdfFilename will be used as the file format
                ImageIOUtil.writeImage(bim, "output/png/" + pdfFilename + "-" + (page + 1) + ".png", 300);
                System.out.println("output/png/" + pdfFilename + "-" + (page + 1) + ".png created");
            }
            document.close();

        } catch (Throwable t) {
            System.out.println(("Loading class failed: [" + t.getClass().getName() + "] " + t.getMessage()));

        }
    /*    } catch (Exception e) {
            System.out.println("extract = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
            fail();
        }*/

    }

    public static LocalDateTime tryToParse(String dateString) {
        LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        return dateTime;
    }


}
