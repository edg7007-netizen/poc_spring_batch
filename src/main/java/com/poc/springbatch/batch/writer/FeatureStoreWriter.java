package com.poc.springbatch.batch.writer;

import com.poc.springbatch.entity.PhoneFeatureStoreEntry;
import com.poc.springbatch.model.PhoneScore;
import com.poc.springbatch.repository.PhoneFeatureStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Writes computed phone scores to the feature store table.
 * Uses upsert logic (delete-then-insert) to avoid duplicates per phone+date.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureStoreWriter implements ItemWriter<PhoneScore> {

    private final PhoneFeatureStoreRepository repository;

    @Override
    public void write(Chunk<? extends PhoneScore> chunk) {
        List<? extends PhoneScore> items = chunk.getItems();
        log.info("FeatureStoreWriter: writing {} items", items.size());

        List<PhoneFeatureStoreEntry> entries = items.stream()
                .map(this::toEntity)
                .toList();

        // Remove existing entries for the same phone+date (upsert via delete+insert)
        entries.forEach(e ->
            repository.findByPhoneNumberAndScoreDate(e.getPhoneNumber(), e.getScoreDate())
                    .ifPresent(existing -> repository.deleteById(existing.getId()))
        );

        repository.saveAll(entries);
        log.info("FeatureStoreWriter: saved {} entries to feature store", entries.size());
    }

    private PhoneFeatureStoreEntry toEntity(PhoneScore score) {
        return PhoneFeatureStoreEntry.builder()
                .phoneNumber(score.getPhoneNumber())
                .userId(score.getUserId())
                .scoreDate(score.getScoreDate())
                .previousLoansCount(score.getPreviousLoansCount())
                .currentBalance(score.getCurrentBalance())
                .appInstalled(score.isAppInstalled())
                .ptpMadeCount(score.getPtpMadeCount())
                .hasBrokenPtp(score.isHasBrokenPtp())
                .daysSinceLastContact(score.getDaysSinceLastContact())
                .historicalAnswerRateSameHour(score.getHistoricalAnswerRateAtSameHour())
                .historicalAnswerRateSameDow(score.getHistoricalAnswerRateAtSameDayOfWeek())
                .reachScore(score.getReachScore())
                .recoveryScore(score.getRecoveryScore())
                .computedAt(score.getComputedAt())
                .build();
    }
}
