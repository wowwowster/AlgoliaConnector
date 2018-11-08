package com.sword.gsa.spis.scs.algolia;

import com.algolia.search.exceptions.AlgoliaException;
import com.algolia.search.objects.Query;
import com.algolia.search.objects.TypoTolerance;
import com.algolia.search.responses.SearchResult;
import com.google.common.collect.Lists;
import com.sword.gsa.spis.scs.service.dto.ActorDTO;
import com.sword.gsa.spis.scs.service.dto.TextBlockDTO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sword.gsa.spis.scs.utils.algolia.AlgoliaUtils.waitForCompletion;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AlgoliaObjectsTest extends AlgoliaTest {

    @Test
    public void getIndex() {
        assertNotNull(initialIndexActor);
    }

   /* @Test
    public void getIndexesContent() throws AlgoliaException {
        SearchResult<ActorDTO> searchResultActor = indexActor.search(new Query("Catherine"));
        assertNotNull(searchResultActor);
        Long nbActor = indexActor.search(new Query("Catherine")).getNbHits();
        assertThat("nbActor", nbActor, greaterThan(Long.valueOf(0)));
    } */

    @Test
    public void getActorWithoutID() throws AlgoliaException {
        String generatedString = "Martin Durand" + UUID.randomUUID().toString();
        ActorDTO actorDTO = new ActorDTO("Martin Durand" + generatedString, "Acteur à supprimer", "/3qDN8It9ulUqpOqkxJgW0WnWFho.jpg", Lists.newArrayList("Film" + generatedString));
        waitForCompletion(indexActor.addObject(actorDTO), client);
        SearchResult<ActorDTO> resultActor = indexActor.search(new Query(generatedString));
        List<ActorDTO> list = resultActor.getHits();

        if (list.isEmpty()) {
            assertThat(list).isEmpty();
        }
    }

   /*  @Test
    public void getActorWithID() throws AlgoliaException {
        String generatedString = "Martin Durand" + UUID.randomUUID().toString();
        ActorWithIdDTO actorWithID = new ActorWithIdDTO("501", "Carl Laurier", "Laurier Carl", "/3qDN8It9ulUqpOqkxJgW0WnWFho.jpg",  Lists.newArrayList("Alien 3"));
        waitForCompletion(initialIndexActorWithID.addObject(actorWithID));
        Optional<ActorWithIdDTO> result = initialIndexActorWithID.getObject("501");
        Assertions.assertThat(actorWithID).isEqualToComparingFieldByField(result.get());
    }

*/
    @Test
    public void testFilters() throws AlgoliaException, InterruptedException {
        SearchResult<TextBlockDTO> textBlocksToDelete = indexTextBlock.search( new Query("").setFacets("document.id").setFilters("document.id:news|21342"));
        assertThat(textBlocksToDelete.getHits()).hasSize(6);
        for (int i = 0; i < 10; i++) {
            List<TextBlockDTO> hits = indexTextBlock.search( new Query("").setFacets("document.id").setFilters("document.id:news|21342")).getHits();
            if (hits.isEmpty()) {
                assertThat(hits).isEmpty();
                return;
            }
            Thread.sleep(1000L);
        }

    }
}
