package com.sword.parser.pdf;

import com.sword.gsa.spis.scs.algolia.AlgoliaTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.fail;

import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
@SpringBootTest
public class PdfToJavaPdfBoxTest extends AlgoliaTest {

    private static final String PDF_FILENAME_TEST_FIRST = "Code-de-deontologie.pdf";
    private static final String PDF_FILENAME_TEST_SECOND =  "le-journal-ordre-pharmaciens-35.pdf";
    private static final String ONE_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologie.xml";
    private static final String BBBBBONE_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologieB.xml";
    private static final String SKIP_TEST_GENERATED_XML_FILENAME = "output/xml/Code-de-deontologie_skipbom.xml";
    private static final String ONE_TEST_GENERATED_JSON_FILENAME = "output/xml/Code-de-deontologie.json";
    private static int PRETTY_PRINT_INDENT_FACTOR = 4;
    private static final String STD_ENCODING = "UTF-8";


    @Test
    public void stripPdfFile() {
   /*     ClassPathResource resource = new ClassPathResource(PDF_FILENAME_TEST_SECOND);

        try (PDDocument document = PDDocument.load(resource.getFile()))
        {
            Writer output = new OutputStreamWriter(new FileOutputStream( "output/txt/Decoupage PdfBox - " +PDF_FILENAME_TEST_SECOND +".txt"), STD_ENCODING );
            String[] args = {"output/txt/Decoupage PdfBox - " +PDF_FILENAME_TEST_SECOND +".txt"};
            PDFDebugger.main(args);


        } catch (Exception je) {
            System.out.println(je.toString());
            fail();
        }*/
    }
}
