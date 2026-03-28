package com.example.phonebatch.service;

import com.example.phonebatch.domain.PhoneNumberRawData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataMergeService {

    public List<PhoneNumberRawData> merge(List<PhoneNumberRawData> apiData,
                                          List<PhoneNumberRawData> dbData,
                                          List<PhoneNumberRawData> s3Data) {
        Map<String, PhoneNumberRawData> map = new LinkedHashMap<>();

        for (PhoneNumberRawData item : apiData) {
            map.put(item.getPhoneNumber(), item);
        }
        for (PhoneNumberRawData item : dbData) {
            PhoneNumberRawData existing = map.computeIfAbsent(item.getPhoneNumber(), PhoneNumberRawData::new);
            existing.setPromiseToPay(item.getPromiseToPay());
            existing.setBrokenPtp(item.isBrokenPtp());
        }
        for (PhoneNumberRawData item : s3Data) {
            PhoneNumberRawData existing = map.computeIfAbsent(item.getPhoneNumber(), PhoneNumberRawData::new);
            existing.setHistoricalAnswerRate(item.getHistoricalAnswerRate());
            existing.setDaysSinceLastContact(item.getDaysSinceLastContact());
            existing.setBalanceOwed(item.getBalanceOwed());
        }

        return new ArrayList<>(map.values());
    }
}
