package com.poc.springbatch.service;

import com.poc.springbatch.batch.reader.DatabasePhoneDataReader;
import com.poc.springbatch.batch.reader.S3PhoneDataReader;
import com.poc.springbatch.model.PhoneRawData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Merges data from the three sources into a single {@link PhoneRawData} object.
 *
 * The API reader is used as the primary source (it drives the list of phone numbers).
 * DB and S3 data are loaded once and used to enrich each API record via lookup maps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataMergeService {

    private final DatabasePhoneDataReader dbReader;
    private final S3PhoneDataReader s3Reader;

    private final Map<String, PhoneRawData> dbDataByPhone = new HashMap<>();
    private final Map<String, PhoneRawData> s3DataByPhone = new HashMap<>();

    @PostConstruct
    public void loadSideData() {
        loadDbData();
        loadS3Data();
    }

    /** Can be called by the batch job to refresh data before each run. */
    public void refresh() {
        dbDataByPhone.clear();
        s3DataByPhone.clear();
        loadDbData();
        loadS3Data();
    }

    /**
     * Enriches an API-sourced {@link PhoneRawData} record with fields from DB and S3.
     */
    public PhoneRawData enrich(PhoneRawData apiRecord) {
        String phone = apiRecord.getPhoneNumber();

        PhoneRawData dbRecord = dbDataByPhone.get(phone);
        PhoneRawData s3Record = s3DataByPhone.get(phone);

        return PhoneRawData.builder()
                // From API
                .phoneNumber(phone)
                .userId(apiRecord.getUserId())
                .previousLoansCount(apiRecord.getPreviousLoansCount())
                .currentBalance(apiRecord.getCurrentBalance())
                .appInstalled(apiRecord.isAppInstalled())
                .fetchedAt(apiRecord.getFetchedAt())
                // From DB replica
                .ptpMadeCount(dbRecord != null ? dbRecord.getPtpMadeCount() : 0)
                .hasBrokenPtp(dbRecord != null && dbRecord.isHasBrokenPtp())
                .lastSuccessfulContactDate(dbRecord != null ? dbRecord.getLastSuccessfulContactDate() : null)
                // From S3
                .historicalAnswerRateAtSameHour(s3Record != null ? s3Record.getHistoricalAnswerRateAtSameHour() : 0.0)
                .historicalAnswerRateAtSameDayOfWeek(s3Record != null ? s3Record.getHistoricalAnswerRateAtSameDayOfWeek() : 0.0)
                .build();
    }

    private void loadDbData() {
        try {
            dbReader.open();
            PhoneRawData item;
            while ((item = dbReader.read()) != null) {
                if (item.getPhoneNumber() != null) {
                    dbDataByPhone.put(item.getPhoneNumber(), item);
                }
            }
            log.info("DataMergeService: loaded {} DB records", dbDataByPhone.size());
        } catch (Exception e) {
            log.error("Failed to load DB data for merge: {}", e.getMessage(), e);
        }
    }

    private void loadS3Data() {
        try {
            s3Reader.open();
            PhoneRawData item;
            while ((item = s3Reader.read()) != null) {
                if (item.getPhoneNumber() != null) {
                    s3DataByPhone.put(item.getPhoneNumber(), item);
                }
            }
            log.info("DataMergeService: loaded {} S3 records", s3DataByPhone.size());
        } catch (Exception e) {
            log.error("Failed to load S3 data for merge: {}", e.getMessage(), e);
        }
    }
}
