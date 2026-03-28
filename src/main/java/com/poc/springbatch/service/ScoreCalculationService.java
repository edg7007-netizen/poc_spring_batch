package com.poc.springbatch.service;

import com.poc.springbatch.model.PhoneRawData;
import com.poc.springbatch.model.PhoneScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Computes the reach score and recovery score from raw phone data.
 *
 * <h3>Reach Score (0–100) – likelihood of reaching the person</h3>
 * <pre>
 *   answerRateSameHour  × 30
 *   answerRateSameDow   × 25
 *   appInstalled bonus  × 20  (boolean → 1.0 or 0.0)
 *   recencyScore        × 25  (0-100 derived from daysSinceContact)
 * </pre>
 *
 * <h3>Recovery Score (0–100) – likelihood of recovering debt</h3>
 * <pre>
 *   balanceScore        × 30  (capped log scale 0-100)
 *   ptpHistoryScore     × 25  (ptpMadeCount normalised 0-100)
 *   brokenPtpPenalty    × 20  (boolean penalty)
 *   appInstalled bonus  × 10
 *   loansScore          × 15  (previousLoansCount normalised 0-100)
 * </pre>
 */
@Slf4j
@Service
public class ScoreCalculationService {

    // --- Reach weights ---
    private static final double W_REACH_ANSWER_RATE_HOUR = 30.0;
    private static final double W_REACH_ANSWER_RATE_DOW  = 25.0;
    private static final double W_REACH_APP_INSTALLED    = 20.0;
    private static final double W_REACH_RECENCY          = 25.0;

    // --- Recovery weights ---
    private static final double W_REC_BALANCE      = 30.0;
    private static final double W_REC_PTP_HISTORY  = 25.0;
    private static final double W_REC_BROKEN_PTP   = 20.0;
    private static final double W_REC_APP_INSTALLED = 10.0;
    private static final double W_REC_LOANS        = 15.0;

    // Thresholds / scaling factors
    private static final double MAX_DAYS_RECENCY   = 180.0;  // 180 days → recencyScore = 0
    private static final double MAX_BALANCE        = 5000.0; // cap for log normalisation
    private static final int    MAX_PTP_COUNT      = 10;
    private static final int    MAX_LOANS_COUNT    = 10;

    public PhoneScore compute(PhoneRawData raw) {
        LocalDate today = LocalDate.now();
        long daysSinceContact = raw.getLastSuccessfulContactDate() != null
                ? ChronoUnit.DAYS.between(raw.getLastSuccessfulContactDate(), today)
                : (long) MAX_DAYS_RECENCY;

        double reachScore    = computeReachScore(raw, daysSinceContact);
        double recoveryScore = computeRecoveryScore(raw);

        log.debug("Computed scores for {}: reach={}, recovery={}", raw.getPhoneNumber(), reachScore, recoveryScore);

        return PhoneScore.builder()
                .phoneNumber(raw.getPhoneNumber())
                .userId(raw.getUserId())
                .scoreDate(today)
                .previousLoansCount(raw.getPreviousLoansCount())
                .currentBalance(raw.getCurrentBalance())
                .appInstalled(raw.isAppInstalled())
                .ptpMadeCount(raw.getPtpMadeCount())
                .hasBrokenPtp(raw.isHasBrokenPtp())
                .daysSinceLastContact(daysSinceContact)
                .historicalAnswerRateAtSameHour(raw.getHistoricalAnswerRateAtSameHour())
                .historicalAnswerRateAtSameDayOfWeek(raw.getHistoricalAnswerRateAtSameDayOfWeek())
                .reachScore(reachScore)
                .recoveryScore(recoveryScore)
                .computedAt(LocalDateTime.now())
                .build();
    }

    // ---------------------------------------------------------------------------
    // Reach
    // ---------------------------------------------------------------------------
    private double computeReachScore(PhoneRawData raw, long daysSinceContact) {
        double hourScore  = clamp01(raw.getHistoricalAnswerRateAtSameHour());
        double dowScore   = clamp01(raw.getHistoricalAnswerRateAtSameDayOfWeek());
        double appScore   = raw.isAppInstalled() ? 1.0 : 0.0;
        double recScore   = recencyScore(daysSinceContact);

        return hourScore  * W_REACH_ANSWER_RATE_HOUR
             + dowScore   * W_REACH_ANSWER_RATE_DOW
             + appScore   * W_REACH_APP_INSTALLED
             + recScore   * W_REACH_RECENCY;
    }

    // ---------------------------------------------------------------------------
    // Recovery
    // ---------------------------------------------------------------------------
    private double computeRecoveryScore(PhoneRawData raw) {
        double balScore    = balanceScore(raw.getCurrentBalance() != null ? raw.getCurrentBalance().doubleValue() : 0.0);
        double ptpScore    = normalise(raw.getPtpMadeCount(), MAX_PTP_COUNT);
        double brokenScore = raw.isHasBrokenPtp() ? 0.0 : 1.0; // no broken PTP → good sign
        double appScore    = raw.isAppInstalled() ? 1.0 : 0.0;
        double loanScore   = normalise(raw.getPreviousLoansCount(), MAX_LOANS_COUNT);

        return balScore    * W_REC_BALANCE
             + ptpScore    * W_REC_PTP_HISTORY
             + brokenScore * W_REC_BROKEN_PTP
             + appScore    * W_REC_APP_INSTALLED
             + loanScore   * W_REC_LOANS;
    }

    // ---------------------------------------------------------------------------
    // Helper normalisation functions
    // ---------------------------------------------------------------------------

    /** Decaying recency: recent contact → score near 1.0, older → near 0.0 */
    private double recencyScore(long days) {
        if (days <= 0) return 1.0;
        if (days >= MAX_DAYS_RECENCY) return 0.0;
        return 1.0 - (days / MAX_DAYS_RECENCY);
    }

    /** Log-normalised balance score: larger balance → higher score (up to 1.0) */
    private double balanceScore(double balance) {
        if (balance <= 0) return 0.0;
        double capped = Math.min(balance, MAX_BALANCE);
        return Math.log1p(capped) / Math.log1p(MAX_BALANCE);
    }

    /** Normalise an integer count to [0, 1] */
    private double normalise(int value, int max) {
        if (max <= 0) return 0.0;
        return clamp01((double) value / max);
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
