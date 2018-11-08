package com.sword.gsa.spis.scs.algolia;

import com.algolia.search.exceptions.AlgoliaException;
import com.algolia.search.objects.Query;
import com.algolia.search.objects.tasks.sync.TaskIndexing;
import com.algolia.search.responses.SearchResult;

import static com.sword.gsa.spis.scs.utils.algolia.AlgoliaUtils.waitForCompletion;
import static org.assertj.core.api.Assertions.assertThat;

import com.sword.gsa.spis.scs.service.dto.ProductDTO;
import com.sword.gsa.spis.scs.service.dto.ProductWithIdDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static com.sword.gsa.spis.scs.utils.lambda.LambdaExceptionWrappers.throwingConsumerWrapper;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductDTOTest extends AlgoliaTest {


    private static final String NEW_BRAND = "Ikea";

    @Before
    public void clearTestingData() throws AlgoliaException {
        SearchResult<ProductWithIdDTO> resultProduct = indexProductWithID.search(new Query(NEW_BRAND));
        List<ProductWithIdDTO> listIkeaProductBeforeAdd = resultProduct.getHits().stream().filter(item -> item.getBrand().equalsIgnoreCase(NEW_BRAND)).collect(Collectors.toList());
        if (listIkeaProductBeforeAdd != null && !listIkeaProductBeforeAdd.isEmpty()) {
            listIkeaProductBeforeAdd.forEach(throwingConsumerWrapper(ikeaProduct -> waitForCompletion(
                    indexProductWithID.deleteObject(ikeaProduct.getObjectID()),client
            )));
        }
    }

    @Test
    public void getIndex() {
        assertNotNull(indexProduct);
    }

    @Test
    public void getIndexesContent() throws AlgoliaException {
        SearchResult<ProductDTO> searchResultProduct = indexProduct.search(new Query("Apple"));
        assertNotNull(searchResultProduct);
        long nbProduct = indexProduct.search(new Query("Apple")).getNbHits();
        assertThat(nbProduct).isGreaterThan(0L);
    }

    @Test
    public void clearTestingDataTest() throws AlgoliaException {
        ProductDTO productDTO = new ProductDTO().setBrand(NEW_BRAND).setDescription("Canapé 2 places, gris").setName("KNOPPARP");
        TaskIndexing taskIndexing = indexProduct.addObject(productDTO);
        waitForCompletion(taskIndexing, client);
        clearTestingData();
        SearchResult<ProductWithIdDTO> resultProduct = indexProductWithID.search(new Query(NEW_BRAND));
        List<ProductWithIdDTO> listIkeaProductBeforeAdd = resultProduct.getHits().stream().filter(item -> item.getBrand().equalsIgnoreCase(NEW_BRAND)).collect(Collectors.toList());
        assertThat(listIkeaProductBeforeAdd.isEmpty());
    }

    @Test
    public void taskIndexingTest() throws AlgoliaException {
        ProductDTO productDTO = new ProductDTO().setBrand(NEW_BRAND).setDescription("Canapé 2 places, gris").setName("KNOPPARP");
        TaskIndexing taskIndexing = indexProduct.addObject(productDTO);
        waitForCompletion(taskIndexing, client);
        //assertThat(listIkeaProductBeforeAdd.size()).isGreaterThan(0);

        waitForCompletion(indexProduct.deleteObject(taskIndexing.getObjectID()), client);
    }

   /*  @Test
    public void getProductWithID() throws AlgoliaException {
        String generatedString = "Martin Durand" + UUID.randomUUID().toString();
        ProductWithIdDTO productWithID = new ProductWithIdDTO("501", "Carl Laurier", "Laurier Carl", "/3qDN8It9ulUqpOqkxJgW0WnWFho.jpg",  Lists.newArrayList("Alien 3"));
        waitForCompletion(initialIndexProductWithID.addObject(productWithID));
        Optional<ProductWithIdDTO> result = initialIndexProductWithID.getObject("501");
        Assertions.assertThat(productWithID).isEqualToComparingFieldByField(result.get());
    }


    @Test
    public void testObjectWithArray() throws AlgoliaException {
        TaskIndexing task =
                initialIndexProduct.addObject(
                        new ProductDTO().setMovies(Arrays.asList("Jean Gabin film 1", "Jean Gabin film 2")).setName("Jean Gabin"));

        waitForCompletion(task);
        ProductDTO obj = initialIndexProduct.getObject(task.getObjectID()).get();
        Assertions.assertThat(obj.getName()).isEqualTo("Jean Gabin");
        Assertions.assertThat(obj.getMovies()).containsOnly("Jean Gabin film 1", "Jean Gabin film 2");
        Long nbProduct = initialIndexProduct.search(new Query("Catherine")).getNbHits();
        String toto = "";
    } */


}
