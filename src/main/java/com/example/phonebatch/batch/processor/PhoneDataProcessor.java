package com.example.phonebatch.batch.processor;

import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import com.example.phonebatch.service.ScoringService;
import com.example.phonebatch.service.ScoringStats;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Scores each phone number using dataset-wide min/max statistics that were
 * pre-computed by {@link com.example.phonebatch.service.ScoringStatsListener}
 * before the step started. This ensures accurate relative normalisation across
 * the full dataset rather than within individual chunks.
 */
@Component
public class PhoneDataProcessor implements ItemProcessor<PhoneNumberRawData, ScoredPhoneNumber> {

    private final ScoringService scoringService;
    private final ScoringStats scoringStats;

    public PhoneDataProcessor(ScoringService scoringService, ScoringStats scoringStats) {
        this.scoringService = scoringService;
        this.scoringStats = scoringStats;
    }

    @Override
    public ScoredPhoneNumber process(PhoneNumberRawData item) {
        return scoringService.score(item,
                scoringStats.getMinLoans(), scoringStats.getMaxLoans(),
                scoringStats.getMinPtp(),   scoringStats.getMaxPtp(),
                scoringStats.getMinBalance(), scoringStats.getMaxBalance());
    }
}
