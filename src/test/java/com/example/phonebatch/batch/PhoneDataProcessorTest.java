package com.example.phonebatch.batch;

import com.example.phonebatch.batch.processor.PhoneDataProcessor;
import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PhoneDataProcessorTest {

    @Autowired
    private PhoneDataProcessor processor;

    @Test
    void testProcessorReturnsScoredPhoneNumber() throws Exception {
        PhoneNumberRawData raw = new PhoneNumberRawData();
        raw.setPhoneNumber("555-0001");
        raw.setPreviousLoans(3);
        raw.setPromiseToPay(2);
        raw.setHistoricalAnswerRate(0.7);
        raw.setDaysSinceLastContact(5);
        raw.setBrokenPtp(false);
        raw.setAppInstalled(true);
        raw.setBalanceOwed(1500.0);

        ScoredPhoneNumber result = processor.process(raw);

        assertThat(result).isNotNull();
        assertThat(result.getReachScore()).isBetween(0.0, 1.0);
        assertThat(result.getRecoveryScore()).isBetween(0.0, 1.0);
    }
}
