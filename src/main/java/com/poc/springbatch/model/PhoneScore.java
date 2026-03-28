package com.poc.springbatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Computed scores and cleaned data ready to be persisted in the feature store.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneScore {

    // --- Identity ---
    private String phoneNumber;
    private String userId;
    private LocalDate scoreDate;

    // --- Raw features (cleaned) ---
    private int previousLoansCount;
    private BigDecimal currentBalance;
    private boolean appInstalled;
    private int ptpMadeCount;
    private boolean hasBrokenPtp;
    private long daysSinceLastContact;      // derived from lastSuccessfulContactDate
    private double historicalAnswerRateAtSameHour;
    private double historicalAnswerRateAtSameDayOfWeek;

    // --- Computed scores (0.0 to 100.0) ---

    /**
     * Reach score: likelihood of successfully reaching the person.
     * Weighted combination of: answer rate at same hour, answer rate at same dow,
     * app installed, recency of last contact.
     */
    private double reachScore;

    /**
     * Recovery score: likelihood of recovering the debt.
     * Weighted combination of: balance, previous loans, PTP history,
     * broken PTP flag, app installed.
     */
    private double recoveryScore;

    private LocalDateTime computedAt;
}
