package com.sword.batch.item;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;

public class LogItemWriter implements ItemWriter<Object> {

    private static final Log log = LogFactory.getLog(LogItemWriter.class);

    public void write(List<? extends Object> data) throws Exception {
        log.error("item dans writer=" + data);
    }

}
