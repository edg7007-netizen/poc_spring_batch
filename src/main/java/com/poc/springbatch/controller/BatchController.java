package com.poc.springbatch.controller;

import com.poc.springbatch.batch.reader.ApiPhoneDataReader;
import com.poc.springbatch.entity.PhoneFeatureStoreEntry;
import com.poc.springbatch.repository.PhoneFeatureStoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Utility endpoints for the batch job and feature store inspection.
 */
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Tag(name = "Batch", description = "Batch job management and feature store queries")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job phoneScoreJob;
    private final PhoneFeatureStoreRepository featureStoreRepository;

    @PostMapping("/run")
    @Operation(summary = "Manually trigger the phone score batch job")
    public ResponseEntity<Map<String, String>> runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(phoneScoreJob, params);
            return ResponseEntity.ok(Map.of("status", "Job started successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "Job failed: " + e.getMessage()));
        }
    }

    @GetMapping("/feature-store")
    @Operation(summary = "List feature store entries for a given date (defaults to today), sorted by reach score desc")
    public ResponseEntity<List<PhoneFeatureStoreEntry>> getFeatureStore(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(featureStoreRepository.findByScoreDateOrderByReachScoreDesc(queryDate));
    }

    @GetMapping("/feature-store/history/{phoneNumber}")
    @Operation(summary = "Get full history for a phone number from the feature store")
    public ResponseEntity<List<PhoneFeatureStoreEntry>> getPhoneHistory(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(featureStoreRepository.findByPhoneNumberOrderByScoreDateDesc(phoneNumber));
    }
}
