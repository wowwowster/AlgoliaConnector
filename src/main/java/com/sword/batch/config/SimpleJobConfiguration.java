package com.sword.batch.config;

import com.sword.domain.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class SimpleJobConfiguration {

    @Bean
    public BatchConfigurer configurer(EntityManagerFactory entityManagerFactory ){
        return new CustomBatchConfigurer( entityManagerFactory );
    }

   /* @Bean
    public SACItemReader reader() {
        return new SACItemReader();
    }*/

    @Bean
    public ItemReader<Document> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Document> itemReader = new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        itemReader.setQueryString("select d from Document d");
        return itemReader;
    }

   /* @Bean
    public LogItemProcessor processor() {
        return new LogItemProcessor();
    }*/

    @Bean
    public ItemProcessor<Document, Document> processor() {
        return item -> {
            //item.setId(0);
            return item;
        };
    }

    /*@Bean
    public LogItemWriter writer() {
        return new LogItemWriter();
    }*/

    @Bean
    public ItemWriter<Document> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Document> itemWriter = new JpaItemWriter<>();
        itemWriter.setEntityManagerFactory(entityManagerFactory);
        return itemWriter;
    }



    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<Document> jpaItemReader, ItemProcessor<Document, Document> processor,
                     ItemWriter<Document> jpaItemWriter) {
        return stepBuilderFactory.get("addAge")
                .<Document, Document>chunk(2)
                .reader(jpaItemReader)
                .processor(processor)
                .writer(jpaItemWriter)
                .build();
    }



    @Bean
    public Job simpleJob(JobBuilderFactory jobBuilderFactory, Step step){
        return jobBuilderFactory.get("simpleJob")
                //TODO à vérifier
                .start(step)
                .build();
    }
}

