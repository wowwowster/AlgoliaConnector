package com.sword.service.batch;

import com.algolia.search.Index;
import com.algolia.search.exceptions.AlgoliaException;
import com.sword.gsa.spis.scs.algolia.AlgoliaConfig;
import com.sword.gsa.spis.scs.service.dto.ParagraphDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private JobLauncher jobLauncher;

    private Job flowJob;

    private static AlgoliaConfig algoliaConfig;

    protected static Index<ParagraphDTO> indexParagraph;

    private static final String PDF_FILENAME = "le-journal-ordre-pharmaciens-35.pdf";

    private static final String HTML_PAGE_NAME = "vinci.html";

    @Autowired
    public JobService(JobLauncher jobLauncher, Job flowJob) {
        this.jobLauncher = jobLauncher;
        this.flowJob = flowJob;
    }

    @Autowired
    public void setAlgoliaConfig(AlgoliaConfig algoliaConfig) {
        JobService.algoliaConfig = algoliaConfig;
    }

    /* fixedRate = 30 secs / initialDelay= 6 minutes

    <second> <minute> <hour> <day-of-month> <month> <day-of-week> <year> <command> *
     @Scheduled(cron = "0/3 * * * * ?") => toutes les 3 secs */
   // @Scheduled(fixedRate = 60 * 60 * 100000, initialDelay= 60 * 60 * 1)
    @Scheduled(cron = "0/30 * * * * ?")
    public void run() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {

        // TODO ajouter un fichier de config logback
        logger.error("début du run");
        //HtmlHandler.extract(HTML_PAGE_NAME);
        //PdfHandler.extract(PDF_FILENAME);
        JobParameters jobParameters = new JobParametersBuilder().addDate("time", new Date()).toJobParameters();
        jobLauncher.run(flowJob, jobParameters);
        logger.error("fin du run");
    }

    protected void clearTestingData() throws AlgoliaException {
        indexParagraph.clear();
    }
}
