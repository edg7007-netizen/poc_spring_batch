package com.example.phonebatch.service;

import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScoringServiceTest {

    @Autowired
    private ScoringService scoringService;

    @Test
    void testScoreAllWithMultipleRecords() {
        PhoneNumberRawData r1 = buildRecord("555-0001", 3, 2, 0.7, 5, false, true, 1200.0);
        PhoneNumberRawData r2 = buildRecord("555-0002", 1, 0, 0.45, 30, false, false, 3400.0);
        PhoneNumberRawData r3 = buildRecord("555-0003", 8, 5, 0.9, 2, true, true, 9000.0);

        List<ScoredPhoneNumber> results = scoringService.scoreAll(List.of(r1, r2, r3));

        assertThat(results).hasSize(3);
        results.forEach(r -> {
            assertThat(r.getReachScore()).isBetween(0.0, 1.0);
            assertThat(r.getRecoveryScore()).isBetween(0.0, 1.0);
        });

        ScoredPhoneNumber scored3 = results.get(2);
        ScoredPhoneNumber scored2 = results.get(1);
        assertThat(scored3.getReachScore()).isGreaterThan(scored2.getReachScore());
    }

    @Test
    void testNormalizationWithSameValues() {
        PhoneNumberRawData r1 = buildRecord("555-0001", 5, 5, 0.5, 10, false, true, 1000.0);
        PhoneNumberRawData r2 = buildRecord("555-0002", 5, 5, 0.5, 10, false, true, 1000.0);

        List<ScoredPhoneNumber> results = scoringService.scoreAll(List.of(r1, r2));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getReachScore()).isEqualTo(results.get(1).getReachScore());
        assertThat(results.get(0).getRecoveryScore()).isEqualTo(results.get(1).getRecoveryScore());
    }

    private PhoneNumberRawData buildRecord(String phone, int prevLoans, int ptp, double har,
                                            int days, boolean brokenPtp, boolean appInstalled,
                                            double balance) {
        PhoneNumberRawData d = new PhoneNumberRawData(phone);
        d.setPreviousLoans(prevLoans);
        d.setPromiseToPay(ptp);
        d.setHistoricalAnswerRate(har);
        d.setDaysSinceLastContact(days);
        d.setBrokenPtp(brokenPtp);
        d.setAppInstalled(appInstalled);
        d.setBalanceOwed(balance);
        return d;
    }
}
