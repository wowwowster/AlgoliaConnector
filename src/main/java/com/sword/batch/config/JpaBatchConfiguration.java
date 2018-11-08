package com.sword.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Configuration
//@EnableBatchProcessing
public class JpaBatchConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JpaBatchConfiguration.class);

  /*  @Bean
    public BatchConfigurer configurer( EntityManagerFactory entityManagerFactory ){
        return new CustomBatchConfigurer( entityManagerFactory );
    }

    @Bean
    public ItemReader<ParagraphDTO> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        ItemReader<ParagraphDTO> itemReader = new ItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        itemReader.setQueryString("select u from JpaUser u");
        return itemReader;
    }

    @Bean
    public ItemProcessor<ParagraphDTO, ParagraphDTO> processor() {
        return item -> {
            item.setAge(item.getAge() + 1);
            item.setDescription("have deal");
            return item;
        };
    }

    @Bean
    public ItemWriter<ParagraphDTO> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<ParagraphDTO> itemWriter = new JpaItemWriter<>();
        itemWriter.setEntityManagerFactory(entityManagerFactory);
        return itemWriter;
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<ParagraphDTO> jpaItemReader, ItemProcessor<ParagraphDTO, ParagraphDTO> processor,
                     ItemWriter<ParagraphDTO> jpaItemWriter) {
        return stepBuilderFactory.get("addAge")
                .<ParagraphDTO, ParagraphDTO>chunk(2)
                .reader(jpaItemReader)
                .processor(processor)
                .writer(jpaItemWriter)
                .build();
    }


    @Bean
    public Job job( JobBuilderFactory jobBuilderFactory, Step step) {
        return jobBuilderFactory.get("addJob")
                .listener(new JobExecutionListener() {
                    private Long time;

                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        time = System.currentTimeMillis();
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        logger.error(String.format("public Job job(Step step) : %sms", System.currentTimeMillis() - time));
                    }
                })
                .flow(step)
                .end()
                .build();
    }
    */
}

