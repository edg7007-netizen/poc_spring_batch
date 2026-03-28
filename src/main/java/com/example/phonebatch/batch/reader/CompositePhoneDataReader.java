package com.example.phonebatch.batch.reader;

import com.example.phonebatch.domain.PhoneNumberRawData;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CompositePhoneDataReader implements ItemReader<PhoneNumberRawData> {

    private final ApiPhoneDataReader apiReader;
    private final DatabasePhoneDataReader dbReader;
    private final S3PhoneDataReader s3Reader;

    private List<PhoneNumberRawData> merged;
    private int index = 0;

    public CompositePhoneDataReader(ApiPhoneDataReader apiReader,
                                     DatabasePhoneDataReader dbReader,
                                     S3PhoneDataReader s3Reader) {
        this.apiReader = apiReader;
        this.dbReader = dbReader;
        this.s3Reader = s3Reader;
    }

    private void initialize() {
        Map<String, PhoneNumberRawData> map = new LinkedHashMap<>();

        for (PhoneNumberRawData item : apiReader.readAll()) {
            map.put(item.getPhoneNumber(), item);
        }
        for (PhoneNumberRawData item : dbReader.readAll()) {
            PhoneNumberRawData existing = map.computeIfAbsent(item.getPhoneNumber(), PhoneNumberRawData::new);
            existing.setPromiseToPay(item.getPromiseToPay());
            existing.setBrokenPtp(item.isBrokenPtp());
        }
        for (PhoneNumberRawData item : s3Reader.readAll()) {
            PhoneNumberRawData existing = map.computeIfAbsent(item.getPhoneNumber(), PhoneNumberRawData::new);
            existing.setHistoricalAnswerRate(item.getHistoricalAnswerRate());
            existing.setDaysSinceLastContact(item.getDaysSinceLastContact());
            existing.setBalanceOwed(item.getBalanceOwed());
        }

        merged = new ArrayList<>(map.values());
        index = 0;
    }

    @Override
    public PhoneNumberRawData read() {
        if (merged == null) {
            initialize();
        }
        if (index < merged.size()) {
            return merged.get(index++);
        }
        // Reset for next job run
        merged = null;
        return null;
    }
}
