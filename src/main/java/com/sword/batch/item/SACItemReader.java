package com.sword.batch.item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemReader;

public class SACItemReader implements ItemReader<String> {

    private static final Log log = LogFactory.getLog(SACItemReader.class);

    private String[] input = {"Good", "morning!", "This", "is", "your", "ItemReader", "speaking!"};
    //private Map<String, Boolean> input = {("Good", false ; , "morning!", false)}
    private static int index = 0;


    public String read() throws Exception {

        while (index < input.length) {
            log.info("début reader read()");
            String item = input[index++];
            log.info("item dans reader=" + item);
            log.info("fin reader read()");
            return item;
        }
        return null;
    }

}



