package com.example.phonebatch.batch.processor;

import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import com.example.phonebatch.service.ScoringService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhoneDataProcessor implements ItemProcessor<PhoneNumberRawData, ScoredPhoneNumber> {

    private final ScoringService scoringService;

    public PhoneDataProcessor(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    /**
     * Processes a single item by scoring it in isolation. Because min/max normalization
     * requires the full dataset, calling scoreAll with a single item means each normalized
     * field defaults to {@link ScoringService#MIDPOINT}. In production, pre-compute
     * dataset-wide min/max stats (e.g. via a JobExecutionListener) and pass them here.
     */
    @Override
    public ScoredPhoneNumber process(PhoneNumberRawData item) {
        return scoringService.scoreAll(List.of(item)).get(0);
    }
}
