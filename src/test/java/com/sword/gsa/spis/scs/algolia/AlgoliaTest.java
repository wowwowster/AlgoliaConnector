package com.sword.gsa.spis.scs.algolia;

import com.algolia.search.APIClient;
import com.algolia.search.Index;
import com.algolia.search.exceptions.AlgoliaException;
import com.sword.gsa.spis.scs.service.dto.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.Security;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AlgoliaTest {

    @Autowired
    private AlgoliaConfig algoliaConfig;

    protected static APIClient client;
    protected static Index<ActorDTO> initialIndexActor;
    protected static Index<ActorWithIdDTO> initialIndexActorWithID;
    protected static Index<ActorDTO> indexActor;
    protected static Index<ActorWithIdDTO> indexActorWithID;
    protected static Index<ProductDTO> indexProduct;
    protected static Index<ProductWithIdDTO> indexProductWithID;
    protected static Index<DocumentDTO> indexDocument;
    protected static Index<TextBlockDTO> indexTextBlock;

    @Before
    public void setIndexes() throws Exception {
        Security.setProperty("networkaddress.cache.ttl", "60");
        client = algoliaConfig.getAPIClient();

        initialIndexActor = client.initIndex("getstarted_actors", ActorDTO.class);
        initialIndexActorWithID = client.initIndex("getstarted_actors", ActorWithIdDTO.class);
        indexActor = client.initIndex("test_actors", ActorDTO.class);
        indexActorWithID = client.initIndex("test_actorsB", ActorWithIdDTO.class);
        indexProduct = client.initIndex("ecommerce", ProductDTO.class);
        indexProductWithID = client.initIndex("ecommerce", ProductWithIdDTO.class);
        indexDocument = client.initIndex("test_documents", DocumentDTO.class);
        indexTextBlock = algoliaConfig.initIndex(TextBlockDTO.class);
    }

    protected void clearTestingData() throws AlgoliaException {
        indexTextBlock.clear();
    }


}
