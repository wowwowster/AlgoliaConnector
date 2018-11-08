package com.sword.batch.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class LogItemProcessor implements ItemProcessor<String,String> {

    private static final Logger logger = LoggerFactory.getLogger(LogItemProcessor.class);

    public String process(String item) throws Exception {
        logger.error("LogItemProcessor item="+item);
        return item;
    }

}
