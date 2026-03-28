package com.example.phonebatch.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job phoneScoreJob;

    public BatchScheduler(JobLauncher jobLauncher, Job phoneScoreJob) {
        this.jobLauncher = jobLauncher;
        this.phoneScoreJob = phoneScoreJob;
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("runTime", LocalDateTime.now())
                .toJobParameters();
            jobLauncher.run(phoneScoreJob, params);
        } catch (Exception e) {
            log.error("Failed to run phoneScoreJob", e);
        }
    }
}
