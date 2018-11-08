package com.sword.gsa.spis.scs.extracting.html;

import com.algolia.search.APIClient;
import com.algolia.search.Index;
import com.sword.gsa.spis.scs.algolia.AlgoliaConfig;
import com.sword.gsa.spis.scs.service.dto.ParagraphDTO;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlHandler {

    private static AlgoliaConfig algoliaConfig;

    @Autowired
    public HtmlHandler(AlgoliaConfig algoliaConfig) {
        HtmlHandler.algoliaConfig = algoliaConfig;
    }

    public static void extract(String htmlPageName) {
        try {
            APIClient apiClient = algoliaConfig.getAPIClient();
            Index<ParagraphDTO> indexParagraph = algoliaConfig.initParsingHTMLIndex(ParagraphDTO.class);
            ClassPathResource resource = new ClassPathResource(htmlPageName);
            PrintWriter writer = new PrintWriter(System.out);
            List<ParagraphDTO> listParagraphDTO = new ArrayList<>();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream(resource.getFile());
            ParseContext pcontext = new ParseContext();
            LocalDateTime lastModified = LocalDateTime.now();
            PrintWriter fileWriter = new PrintWriter("output/txt/resultatsTika " + htmlPageName + ".doc");
            HtmlParser htmlparser = new HtmlParser();
            htmlparser.parse(inputstream, handler, metadata,pcontext);
            String[] metadataNames = metadata.names();

            System.out.println("Contents of the document:" + handler.toString());

            /*  Document doc = Jsoup.connect("http://news.ycombinator.com/").timeout(10*1000).get(); // 10ms
            Element body = doc.body();

            Elements pages = body.children();
            int insertNumber = 0;
            for (int indexPage = 1; indexPage < pages.size(); indexPage++) {

                Elements paragraphs = pages.get(indexPage - 1).getElementsByTag("p");
                String lastLinkText = "";
                for (Element paragraph : paragraphs) {
                    String paragraphText = paragraph.text().replaceAll("- ", "");
                    if (paragraphText != null && paragraphText.length() > 0  //14
                            && !paragraphText.equalsIgnoreCase(lastLinkText)) {
                        fileWriter.println(paragraphText);
                        fileWriter.println("");
                        lastLinkText = paragraphText;
                        WebPageDTO newWebPage = new WebPageDTO().setUrl(htmlPageName).setIndexationDate(lastModified);
                        listParagraphDTO.add(new ParagraphDTO().setWebPage(newWebPage).setInsertNumber(++insertNumber).setPageNumber(indexPage).setContent(paragraphText).setObjectID(UUID.randomUUID().toString()));
                    }
                }
            }

            // TODO contrôler la valeur de last modified
            waitForCompletion(indexParagraph.addObjects(listParagraphDTO), apiClient);
            System.out.println("tout va bien=" + apiClient.toString());  */
        } catch (Exception e) {
            System.out.println("extract = " + e.getMessage());
            e.printStackTrace();
            System.out.println("tout va mal");
        }
    }

    public String convertUrlToString(String stringUrl)  {
        StringBuffer sb = new StringBuffer();
            try {
                String webPage = stringUrl;
                URL url = new URL(webPage);
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                int numCharsRead;
                char[] charArray = new char[1024];

                while ((numCharsRead = isr.read(charArray)) > 0) {
                    sb.append(charArray, 0, numCharsRead);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return sb.toString();
    }
}
