package com.poc.springbatch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for the lightweight feature store.
 * Each row represents all features and scores for a phone number on a given date,
 * providing a full audit trail.
 */
@Entity
@Table(
    name = "phone_feature_store",
    indexes = {
        @Index(name = "idx_pfs_phone_date", columnList = "phone_number, score_date"),
        @Index(name = "idx_pfs_user_date",  columnList = "user_id, score_date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneFeatureStoreEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    // --- Raw features ---
    @Column(name = "previous_loans_count")
    private int previousLoansCount;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "app_installed")
    private boolean appInstalled;

    @Column(name = "ptp_made_count")
    private int ptpMadeCount;

    @Column(name = "has_broken_ptp")
    private boolean hasBrokenPtp;

    @Column(name = "days_since_last_contact")
    private long daysSinceLastContact;

    @Column(name = "historical_answer_rate_same_hour")
    private double historicalAnswerRateSameHour;

    @Column(name = "historical_answer_rate_same_dow")
    private double historicalAnswerRateSameDow;

    // --- Computed scores ---
    @Column(name = "reach_score")
    private double reachScore;

    @Column(name = "recovery_score")
    private double recoveryScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "computed_at")
    private LocalDateTime computedAt;
}
