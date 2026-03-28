package com.poc.springbatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Raw data collected from all three sources for a given phone number.
 * Sources: API endpoint, database read replica, S3 files.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRawData {

    // --- Identity ---
    private String phoneNumber;
    private String userId;

    // --- From API source (loan/account service) ---
    private int previousLoansCount;
    private BigDecimal currentBalance;
    private boolean appInstalled;

    // --- From Database read-replica (collections service) ---
    private int ptpMadeCount;           // number of promises-to-pay made
    private boolean hasBrokenPtp;       // has ever broken a PTP
    private LocalDate lastSuccessfulContactDate; // recency of last contact

    // --- From S3 files (dialler analytics) ---
    private double historicalAnswerRateAtSameHour;   // 0.0 to 1.0
    private double historicalAnswerRateAtSameDayOfWeek; // 0.0 to 1.0

    // --- Metadata ---
    private LocalDateTime fetchedAt;
}
