package com.poc.springbatch.service;

import com.poc.springbatch.model.PhoneRawData;
import com.poc.springbatch.model.PhoneScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculationServiceTest {

    private ScoreCalculationService service;

    @BeforeEach
    void setUp() {
        service = new ScoreCalculationService();
    }

    @Test
    void compute_withFullData_returnsBothScoresInRange() {
        PhoneRawData raw = PhoneRawData.builder()
                .phoneNumber("5551001001")
                .userId("U001")
                .previousLoansCount(3)
                .currentBalance(new BigDecimal("1500.00"))
                .appInstalled(true)
                .ptpMadeCount(2)
                .hasBrokenPtp(false)
                .lastSuccessfulContactDate(LocalDate.now().minusDays(5))
                .historicalAnswerRateAtSameHour(0.72)
                .historicalAnswerRateAtSameDayOfWeek(0.68)
                .build();

        PhoneScore score = service.compute(raw);

        assertThat(score.getPhoneNumber()).isEqualTo("5551001001");
        assertThat(score.getReachScore()).isBetween(0.0, 100.0);
        assertThat(score.getRecoveryScore()).isBetween(0.0, 100.0);
        assertThat(score.getDaysSinceLastContact()).isEqualTo(5L);
    }

    @Test
    void compute_withHighAnswerRateAndAppInstalled_givesHighReachScore() {
        PhoneRawData raw = PhoneRawData.builder()
                .phoneNumber("5551001002")
                .historicalAnswerRateAtSameHour(1.0)
                .historicalAnswerRateAtSameDayOfWeek(1.0)
                .appInstalled(true)
                .lastSuccessfulContactDate(LocalDate.now().minusDays(1))
                .currentBalance(BigDecimal.ZERO)
                .build();

        PhoneScore score = service.compute(raw);

        // Max possible reach: 30 + 25 + 20 + ~25 = ~100
        assertThat(score.getReachScore()).isGreaterThan(90.0);
    }

    @Test
    void compute_withNoContact_givesLowReachScore() {
        PhoneRawData raw = PhoneRawData.builder()
                .phoneNumber("5551001003")
                .historicalAnswerRateAtSameHour(0.0)
                .historicalAnswerRateAtSameDayOfWeek(0.0)
                .appInstalled(false)
                .lastSuccessfulContactDate(null)
                .currentBalance(BigDecimal.ZERO)
                .build();

        PhoneScore score = service.compute(raw);

        assertThat(score.getReachScore()).isEqualTo(0.0);
    }

    @Test
    void compute_withHighBalance_givesHighRecoveryScore() {
        PhoneRawData raw = PhoneRawData.builder()
                .phoneNumber("5551001004")
                .currentBalance(new BigDecimal("5000.00"))
                .previousLoansCount(10)
                .ptpMadeCount(10)
                .hasBrokenPtp(false)
                .appInstalled(true)
                .historicalAnswerRateAtSameHour(0.5)
                .historicalAnswerRateAtSameDayOfWeek(0.5)
                .build();

        PhoneScore score = service.compute(raw);

        assertThat(score.getRecoveryScore()).isGreaterThan(70.0);
    }

    @Test
    void compute_withBrokenPtp_reducesRecoveryScore() {
        PhoneRawData brokenPtpRaw = PhoneRawData.builder()
                .phoneNumber("5551001005")
                .currentBalance(new BigDecimal("1000.00"))
                .hasBrokenPtp(true)
                .build();

        PhoneRawData noBrokenPtpRaw = PhoneRawData.builder()
                .phoneNumber("5551001006")
                .currentBalance(new BigDecimal("1000.00"))
                .hasBrokenPtp(false)
                .build();

        PhoneScore broken    = service.compute(brokenPtpRaw);
        PhoneScore notBroken = service.compute(noBrokenPtpRaw);

        assertThat(broken.getRecoveryScore()).isLessThan(notBroken.getRecoveryScore());
    }

    @Test
    void compute_scoreDateIsToday() {
        PhoneRawData raw = PhoneRawData.builder()
                .phoneNumber("5551001007")
                .build();

        PhoneScore score = service.compute(raw);

        assertThat(score.getScoreDate()).isEqualTo(LocalDate.now());
    }
}
