package com.sword.gsa.spis.scs.utils.algolia;

import com.algolia.search.APIClient;
import com.algolia.search.Index;
import com.algolia.search.exceptions.AlgoliaException;
import com.algolia.search.objects.tasks.sync.GenericTask;
import com.algolia.search.objects.tasks.sync.TaskIndexing;

public class AlgoliaUtils {

    private static final long WAIT_TIME_IN_SECONDS = 60 * 5; // 5 minutes

    public static <T> void waitForCompletion(GenericTask<T> task, APIClient client) throws AlgoliaException {
        client.waitTask(task, WAIT_TIME_IN_SECONDS);
    }

    public <T> void deleteAlgoliaObject(final Index<T> index, final T t, TaskIndexing task, APIClient client) throws AlgoliaException{
        waitForCompletion(index.deleteObject(task.getObjectID()), client);
    }
}

