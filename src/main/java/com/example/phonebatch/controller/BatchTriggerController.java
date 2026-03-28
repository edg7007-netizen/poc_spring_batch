package com.example.phonebatch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Provides a manual HTTP trigger for the batch job so developers can test
 * without waiting for the 5:00 AM cron schedule.
 */
@RestController
@RequestMapping("/api/v1")
public class BatchTriggerController {

    private static final Logger log = LoggerFactory.getLogger(BatchTriggerController.class);

    private final JobLauncher jobLauncher;
    private final Job phoneScoreJob;

    public BatchTriggerController(JobLauncher jobLauncher, Job phoneScoreJob) {
        this.jobLauncher = jobLauncher;
        this.phoneScoreJob = phoneScoreJob;
    }

    @PostMapping("/batch/run")
    public ResponseEntity<Map<String, String>> triggerJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("runTime", LocalDateTime.now())
                .toJobParameters();
            jobLauncher.run(phoneScoreJob, params);
            return ResponseEntity.ok(Map.of("status", "COMPLETED", "message", "Batch job executed successfully"));
        } catch (Exception e) {
            log.error("Manual batch trigger failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "FAILED", "message", e.getMessage()));
        }
    }
}
