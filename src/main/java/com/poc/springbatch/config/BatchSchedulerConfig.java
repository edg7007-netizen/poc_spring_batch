package com.poc.springbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler that triggers the phone score batch job daily at 05:00 AM.
 *
 * The cron expression {@code 0 0 5 * * ?} means:
 *   second=0, minute=0, hour=5, day=every, month=every, weekday=every.
 *
 * To override the schedule at runtime set:
 *   {@code app.batch.schedule.cron=0 0 5 * * ?}
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchSchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job phoneScoreJob;

    @Scheduled(cron = "${app.batch.schedule.cron:0 0 5 * * ?}")
    public void runDailyPhoneScoreJob() {
        log.info("Scheduler triggered: starting phoneScoreJob");
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(phoneScoreJob, params);
            log.info("Scheduler: phoneScoreJob completed");
        } catch (Exception e) {
            log.error("Scheduler: phoneScoreJob failed", e);
        }
    }
}
