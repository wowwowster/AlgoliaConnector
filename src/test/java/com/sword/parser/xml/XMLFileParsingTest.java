package com.sword.parser.xml;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sword.ParsingHandler;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;
import org.apache.tika.metadata.Property;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class XMLFileParsingTest  {

    private Property PARAGRAPH = Property.internalTextBag("paragraph");

    @Test
    public void parseXMLDocument() throws Exception {
        try (InputStream input = XMLFileParsingTest.class.getResourceAsStream("/testXML.xml")) {
              ParsingHandler.parseXML(input);

        }
    }
}
