package com.poc.springbatch.batch.processor;

import com.poc.springbatch.model.PhoneRawData;
import com.poc.springbatch.model.PhoneScore;
import com.poc.springbatch.service.DataMergeService;
import com.poc.springbatch.service.ScoreCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Spring Batch processor: merges data from all three sources and computes scores.
 *
 * The processor receives a {@link PhoneRawData} item that has already been merged
 * by {@link DataMergeService} and delegates score computation to
 * {@link ScoreCalculationService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhoneScoreProcessor implements ItemProcessor<PhoneRawData, PhoneScore> {

    private final DataMergeService dataMergeService;
    private final ScoreCalculationService scoreCalculationService;

    @Override
    public PhoneScore process(PhoneRawData item) {
        log.debug("Processing phone number: {}", item.getPhoneNumber());
        PhoneRawData merged = dataMergeService.enrich(item);
        return scoreCalculationService.compute(merged);
    }
}
