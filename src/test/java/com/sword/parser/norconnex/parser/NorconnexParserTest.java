package com.sword.parser.norconnex.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.norconex.importer.handler.tagger.impl.DOMTagger;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

import com.norconex.importer.doc.ImporterMetadata;
import com.norconex.importer.handler.ImporterHandlerException;
import com.norconex.importer.handler.tagger.impl.DOMTagger.DOMExtractDetails;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NorconnexParserTest {


    private String[] getSortedArray(ImporterMetadata metadata, String key) {
        List<String> list = metadata.getStrings(key);
        Collections.sort(list);
        return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private String cleanHTML(String html) {
        String clean = html;
        clean = clean.replaceAll("[\\r\\n]", "");
        clean = clean.replaceAll(">\\s+<", "><");
        return clean;
    }

    @Test
    public void testAllExtractionTypes()
            throws IOException, ImporterHandlerException {


        DOMTagger t = new DOMTagger();
        t.addDOMExtractDetails(new DOMExtractDetails(
                "div.parent", "text", false, "text"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "span.child1", "html", false, "html"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "span.child1", "outerHtml", false, "outerHtml"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "script", "data", false, "data"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "div.parent", "id", false, "id"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "div.parent", "ownText", false, "ownText"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "div.parent", "tagName", false, "tagName"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                ".formElement", "val", false, "val"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "textarea", "className", false, "className"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                ".child2", "cssSelector", false, "cssSelector"));
        t.addDOMExtractDetails(new DOMExtractDetails(
                "textarea", "attr", false, "attr(title)"));

        String content = "<html><body>"
                + "<script>This is data, not HTML.</script>"
                + "<div id=\"content\" class=\"parent\">Parent text."
                + "<span class=\"child1\">Child text <b>1</b>.</span>"
                + "<span class=\"child2\">Child text <b>2</b>.</span>"
                + "</div>"
                + "<textarea class=\"formElement\" title=\"Some Title\">"
                + "textarea value.</textarea>"
                + "</body></html>";

        ImporterMetadata metadata = new ImporterMetadata();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        metadata.setString(ImporterMetadata.DOC_CONTENT_TYPE, "text/html");
        t.tagDocument("n/a", is, metadata, false);
        is.close();

        String text = metadata.getString("text");
        String html = metadata.getString("html");
        String outerHtml = metadata.getString("outerHtml");
        String data = metadata.getString("data");
        String id = metadata.getString("id");
        String ownText = metadata.getString("ownText");
        String tagName = metadata.getString("tagName");
        String val = metadata.getString("val");
        String className = metadata.getString("className");
        String cssSelector = metadata.getString("cssSelector");
        String attr = metadata.getString("attr");

        Assert.assertEquals("Parent text.Child text 1.Child text 2.", text);
        Assert.assertEquals("Child text <b>1</b>.", html);
        Assert.assertEquals(
                "<span class=\"child1\">Child text <b>1</b>.</span>",
                outerHtml);
        Assert.assertEquals("This is data, not HTML.", data);
        Assert.assertEquals("content", id);
        Assert.assertEquals("Parent text.", ownText);
        Assert.assertEquals("div", tagName);
        Assert.assertEquals("textarea value.", val);
        Assert.assertEquals("formElement", className);
        Assert.assertEquals("#content > span.child2", cssSelector);
        Assert.assertEquals("Some Title", attr);
    }

}
