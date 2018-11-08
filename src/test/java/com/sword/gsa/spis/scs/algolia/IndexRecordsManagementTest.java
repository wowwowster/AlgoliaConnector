package com.sword.gsa.spis.scs.algolia;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(SpringJUnit4ClassRunner.class)

public class IndexRecordsManagementTest {

   /* @Test
    public void addAttributeToOneRecord() throws AlgoliaException {

        SearchResult<ActorWithIdDTO> searchResultActorBeforeUpdate = indexActorWithID.search(new Query("Colin Farrell"));
        List<ActorWithIdDTO> actorWithIDListToUpdate = searchResultActorBeforeUpdate.getHits();
        ActorWithIdDTO actorToUpdate = actorWithIDListToUpdate.get(0);
        actorToUpdate.setMovies(Collections.emptyList());
        indexActorWithID.saveObject(actorToUpdate.getObjectID(), actorToUpdate);

        ActorWithIdDTO actorToControl = indexActorWithID.search(new Query("Colin Farrell")).getHits().get(0);

        assertThat(actorToControl.getMovies() != null);
    }

    @Test
    public void addAttributeeToEveryRecord() throws AlgoliaException {

    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
    @Test
    public void copyIndex() throws AlgoliaException {

        waitForCompletion(initialIndexActorWithID.copyTo(indexActorWithID.getName()));
        long initialNbActor = initialIndexActor.search(new Query("")).getNbHits();
        long nbActor = indexActor.search(new Query("")).getNbHits();
        assertThat(initialNbActor == nbActor);

    } */

}
