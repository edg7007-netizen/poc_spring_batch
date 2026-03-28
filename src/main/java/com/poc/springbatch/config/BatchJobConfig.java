package com.poc.springbatch.config;

import com.poc.springbatch.batch.processor.PhoneScoreProcessor;
import com.poc.springbatch.batch.reader.ApiPhoneDataReader;
import com.poc.springbatch.batch.writer.FeatureStoreWriter;
import com.poc.springbatch.dialer.DialerService;
import com.poc.springbatch.entity.PhoneFeatureStoreEntry;
import com.poc.springbatch.model.PhoneRawData;
import com.poc.springbatch.model.PhoneScore;
import com.poc.springbatch.repository.PhoneFeatureStoreRepository;
import com.poc.springbatch.service.DataMergeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Batch job configuration.
 *
 * <pre>
 * Job: phoneScoreJob
 *   Step 1: refreshDataStep      – refreshes DB/S3 side-data lookup maps
 *   Step 2: scoreAndStoreStep    – reads API data, merges, scores, writes to feature store
 *   Step 3: dispatchDialerStep   – sorts by reach score, sends to dialler
 * </pre>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApiPhoneDataReader apiPhoneDataReader;
    private final PhoneScoreProcessor phoneScoreProcessor;
    private final FeatureStoreWriter featureStoreWriter;
    private final DialerService dialerService;
    private final DataMergeService dataMergeService;
    private final PhoneFeatureStoreRepository featureStoreRepository;

    @Bean
    public Job phoneScoreJob() {
        return new JobBuilder("phoneScoreJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(refreshDataStep())
                .next(scoreAndStoreStep())
                .next(dispatchDialerStep())
                .build();
    }

    // -----------------------------------------------------------------------
    // Step 1: Refresh lookup data from DB replica and S3
    // -----------------------------------------------------------------------
    @Bean
    public Step refreshDataStep() {
        return new StepBuilder("refreshDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Step 1: refreshing DB and S3 side data");
                    dataMergeService.refresh();
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // -----------------------------------------------------------------------
    // Step 2: Score & store
    // -----------------------------------------------------------------------
    @Bean
    public Step scoreAndStoreStep() {
        return new StepBuilder("scoreAndStoreStep", jobRepository)
                .<PhoneRawData, PhoneScore>chunk(50, transactionManager)
                .reader(apiPhoneDataReader)
                .processor(phoneScoreProcessor)
                .writer(featureStoreWriter)
                .build();
    }

    // -----------------------------------------------------------------------
    // Step 3: Sort by reach score, send to dialler
    // -----------------------------------------------------------------------
    @Bean
    public Step dispatchDialerStep() {
        return new StepBuilder("dispatchDialerStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Step 3: dispatching sorted phone list to dialler");
                    LocalDate today = LocalDate.now();
                    List<PhoneFeatureStoreEntry> entries =
                            featureStoreRepository.findByScoreDateOrderByReachScoreDesc(today);

                    List<PhoneScore> scores = entries.stream()
                            .map(e -> PhoneScore.builder()
                                    .phoneNumber(e.getPhoneNumber())
                                    .userId(e.getUserId())
                                    .scoreDate(e.getScoreDate())
                                    .reachScore(e.getReachScore())
                                    .recoveryScore(e.getRecoveryScore())
                                    .build())
                            .toList();

                    dialerService.dispatch(scores);
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
