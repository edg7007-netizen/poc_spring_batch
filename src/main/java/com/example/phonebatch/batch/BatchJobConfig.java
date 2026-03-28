package com.example.phonebatch.batch;

import com.example.phonebatch.batch.processor.PhoneDataProcessor;
import com.example.phonebatch.batch.reader.CompositePhoneDataReader;
import com.example.phonebatch.batch.writer.FeatureStoreWriter;
import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import com.example.phonebatch.service.ScoringStats;
import com.example.phonebatch.service.ScoringStatsListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJobConfig {

    @Bean
    public ScoringStats scoringStats() {
        return new ScoringStats();
    }

    @Bean
    public Job phoneScoreJob(JobRepository jobRepository, Step phoneScoreStep) {
        return new JobBuilder("phoneScoreJob", jobRepository)
            .start(phoneScoreStep)
            .build();
    }

    @Bean
    public Step phoneScoreStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                CompositePhoneDataReader reader,
                                PhoneDataProcessor processor,
                                FeatureStoreWriter writer,
                                ScoringStatsListener scoringStatsListener) {
        return new StepBuilder("phoneScoreStep", jobRepository)
            .<PhoneNumberRawData, ScoredPhoneNumber>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(scoringStatsListener)
            .build();
    }
}
