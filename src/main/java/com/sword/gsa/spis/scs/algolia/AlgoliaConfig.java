package com.sword.gsa.spis.scs.algolia;

import com.algolia.search.APIClient;
import com.algolia.search.ApacheAPIClientBuilder;
import com.algolia.search.Index;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class AlgoliaConfig {

    @Value("${algolia.application.id}")
    public String APPLICATION_ID = "47CSD2QLER";

    @Value("${algolia.apikey}")
    public String API_KEY = "b77f1a0f29c4243afda6282330456514";

    @Value("${algolia.parsing.pdf.index.name}")
    public String parsingPDFIndexName = "pdf_paragraphs";

    @Value("${algolia.parsing.html.index.name}")
    public String parsingHTMLIndexName = "html_paragraphs";

    public String indexName = "dev_textblocks";

    private static APIClient apiClient = null;

    public final synchronized APIClient getAPIClient() {
        if (apiClient == null) {
            return new ApacheAPIClientBuilder(APPLICATION_ID, API_KEY).setConnectTimeout(10000).build();
        }
        return apiClient;
    }

    public <T> Index<T> initIndex(@Nonnull Class<T> klass)
    {
        APIClient apiClient = createInstance(APPLICATION_ID, API_KEY);
        /** TODO @author claurier
         Récupérer le nom de l'index par annotation afin d'avoir une méthode plus générique
         */
        return apiClient.initIndex(indexName, klass);
    }

    public <T> Index<T> initParsingPDFIndex(@Nonnull Class<T> klass)
    {
        APIClient apiClient = createInstance(APPLICATION_ID, API_KEY);
        return apiClient.initIndex(parsingPDFIndexName, klass);
    }

    public <T> Index<T> initParsingHTMLIndex(@Nonnull Class<T> klass)
    {
        APIClient apiClient = createInstance(APPLICATION_ID, API_KEY);
        return apiClient.initIndex(parsingHTMLIndexName, klass);
    }

    protected static APIClient createInstance(String appId, String apiKey) {
        return new ApacheAPIClientBuilder(appId, apiKey).setConnectTimeout(10000).build();
    }

}
