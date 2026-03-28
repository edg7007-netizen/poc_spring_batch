package com.example.phonebatch.service;

import com.example.phonebatch.batch.reader.ApiPhoneDataReader;
import com.example.phonebatch.batch.reader.DatabasePhoneDataReader;
import com.example.phonebatch.batch.reader.S3PhoneDataReader;
import com.example.phonebatch.domain.PhoneNumberRawData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Computes dataset-wide min/max statistics before the scoring step starts.
 * This ensures that {@link ScoringService} normalises against the full dataset
 * rather than a single chunk, giving accurate relative scores.
 */
@Component
public class ScoringStatsListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ScoringStatsListener.class);

    private final ApiPhoneDataReader apiReader;
    private final DatabasePhoneDataReader dbReader;
    private final S3PhoneDataReader s3Reader;
    private final DataMergeService dataMergeService;
    private final ScoringStats scoringStats;

    public ScoringStatsListener(ApiPhoneDataReader apiReader,
                                 DatabasePhoneDataReader dbReader,
                                 S3PhoneDataReader s3Reader,
                                 DataMergeService dataMergeService,
                                 ScoringStats scoringStats) {
        this.apiReader = apiReader;
        this.dbReader = dbReader;
        this.s3Reader = s3Reader;
        this.dataMergeService = dataMergeService;
        this.scoringStats = scoringStats;
    }

    @BeforeStep
    @Override
    public void beforeStep(StepExecution stepExecution) {
        List<PhoneNumberRawData> all = dataMergeService.merge(
                apiReader.readAll(), dbReader.readAll(), s3Reader.readAll());

        scoringStats.setMinLoans(all.stream().mapToInt(PhoneNumberRawData::getPreviousLoans).min().orElse(0));
        scoringStats.setMaxLoans(all.stream().mapToInt(PhoneNumberRawData::getPreviousLoans).max().orElse(0));
        scoringStats.setMinPtp(all.stream().mapToInt(PhoneNumberRawData::getPromiseToPay).min().orElse(0));
        scoringStats.setMaxPtp(all.stream().mapToInt(PhoneNumberRawData::getPromiseToPay).max().orElse(0));
        scoringStats.setMinBalance(all.stream().mapToDouble(PhoneNumberRawData::getBalanceOwed).min().orElse(0));
        scoringStats.setMaxBalance(all.stream().mapToDouble(PhoneNumberRawData::getBalanceOwed).max().orElse(0));

        log.info("ScoringStats computed: loans=[{},{}], ptp=[{},{}], balance=[{},{}]",
                scoringStats.getMinLoans(), scoringStats.getMaxLoans(),
                scoringStats.getMinPtp(), scoringStats.getMaxPtp(),
                scoringStats.getMinBalance(), scoringStats.getMaxBalance());
    }
}
