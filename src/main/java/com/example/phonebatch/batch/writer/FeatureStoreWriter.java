package com.example.phonebatch.batch.writer;

import com.example.phonebatch.dialer.DialerStrategy;
import com.example.phonebatch.domain.FeatureStoreEntry;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import com.example.phonebatch.repository.FeatureStoreRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class FeatureStoreWriter implements ItemWriter<ScoredPhoneNumber> {

    private final FeatureStoreRepository repository;
    private final DialerStrategy dialerStrategy;

    public FeatureStoreWriter(FeatureStoreRepository repository, DialerStrategy dialerStrategy) {
        this.repository = repository;
        this.dialerStrategy = dialerStrategy;
    }

    @Override
    public void write(Chunk<? extends ScoredPhoneNumber> chunk) {
        List<ScoredPhoneNumber> items = new ArrayList<>(chunk.getItems());

        items.forEach(scored -> {
            FeatureStoreEntry entry = mapToEntry(scored);
            entry.setJobDate(LocalDateTime.now());
            repository.save(entry);
        });

        items.sort(Comparator.comparingDouble(ScoredPhoneNumber::getReachScore).reversed());
        dialerStrategy.sendPhoneList(items);
    }

    private FeatureStoreEntry mapToEntry(ScoredPhoneNumber scored) {
        FeatureStoreEntry entry = new FeatureStoreEntry();
        var raw = scored.getRawData();
        entry.setPhoneNumber(raw.getPhoneNumber());
        entry.setPreviousLoans(raw.getPreviousLoans());
        entry.setPromiseToPay(raw.getPromiseToPay());
        entry.setHistoricalAnswerRate(raw.getHistoricalAnswerRate());
        entry.setDaysSinceLastContact(raw.getDaysSinceLastContact());
        entry.setBrokenPtp(raw.isBrokenPtp());
        entry.setAppInstalled(raw.isAppInstalled());
        entry.setBalanceOwed(raw.getBalanceOwed());
        entry.setReachScore(scored.getReachScore());
        entry.setRecoveryScore(scored.getRecoveryScore());
        return entry;
    }
}
