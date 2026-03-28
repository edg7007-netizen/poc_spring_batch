package com.poc.springbatch.service;

import com.poc.springbatch.ai.LlmClient;
import com.poc.springbatch.entity.PhoneFeatureStoreEntry;
import com.poc.springbatch.repository.PhoneFeatureStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Generates a negotiation cheat sheet for a phone agent by:
 * 1. Fetching the latest feature store data for the given phone number or user ID.
 * 2. Building a prompt with the client's feature data.
 * 3. Calling the configured {@link LlmClient} (AWS Bedrock or mock) for strategic advice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheatSheetService {

    private final PhoneFeatureStoreRepository featureStoreRepository;
    private final LlmClient llmClient;

    public String generateByPhoneNumber(String phoneNumber) {
        Optional<PhoneFeatureStoreEntry> entry =
                featureStoreRepository.findTopByPhoneNumberOrderByScoreDateDesc(phoneNumber);
        return entry.map(this::generate)
                .orElse("No data found for phone number: " + phoneNumber);
    }

    public String generateByUserId(String userId) {
        Optional<PhoneFeatureStoreEntry> entry =
                featureStoreRepository.findTopByUserIdOrderByScoreDateDesc(userId);
        return entry.map(this::generate)
                .orElse("No data found for user ID: " + userId);
    }

    String generate(PhoneFeatureStoreEntry entry) {
        log.info("Generating cheat sheet for phone={} date={}", entry.getPhoneNumber(), entry.getScoreDate());
        String prompt = buildPrompt(entry);
        try {
            return llmClient.chat(prompt);
        } catch (Exception e) {
            log.error("LLM call failed for phone={}: {}", entry.getPhoneNumber(), e.getMessage());
            return buildFallbackCheatSheet(entry);
        }
    }

    String buildPrompt(PhoneFeatureStoreEntry e) {
        return String.format(
            "You are an expert debt recovery advisor helping a phone agent prepare for a collection call.%n%n"
          + "CLIENT PROFILE%n"
          + "---------------%n"
          + "Phone Number      : %s%n"
          + "User ID           : %s%n"
          + "Profile Date      : %s%n%n"
          + "FINANCIAL PROFILE%n"
          + "Current Balance   : $%s%n"
          + "Previous Loans    : %d%n%n"
          + "PTP HISTORY%n"
          + "PTPs Made         : %d%n"
          + "Broken PTP        : %s%n%n"
          + "ENGAGEMENT SIGNALS%n"
          + "App Installed     : %s%n"
          + "Days Since Contact: %d%n"
          + "Answer Rate (hour): %.0f%%%n"
          + "Answer Rate (dow) : %.0f%%%n%n"
          + "SCORES%n"
          + "Reach Score       : %.1f / 100%n"
          + "Recovery Score    : %.1f / 100%n%n"
          + "Please provide a concise negotiation cheat sheet (max 400 words) including:%n"
          + "1. Opening line  2. Key talking points  3. Negotiation strategy%n"
          + "4. Likely objections and rebuttals  5. Red flags  6. Closing tip",
            e.getPhoneNumber(),
            e.getUserId() != null ? e.getUserId() : "N/A",
            e.getScoreDate(),
            e.getCurrentBalance() != null ? e.getCurrentBalance().toPlainString() : "0",
            e.getPreviousLoansCount(),
            e.getPtpMadeCount(),
            e.isHasBrokenPtp() ? "Yes" : "No",
            e.isAppInstalled() ? "Yes" : "No",
            e.getDaysSinceLastContact(),
            e.getHistoricalAnswerRateSameHour() * 100,
            e.getHistoricalAnswerRateSameDow() * 100,
            e.getReachScore(),
            e.getRecoveryScore()
        );
    }

    String buildFallbackCheatSheet(PhoneFeatureStoreEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== NEGOTIATION CHEAT SHEET (fallback) ===\n\n");
        sb.append(String.format("Phone: %s  |  User: %s  |  Date: %s%n",
                e.getPhoneNumber(), e.getUserId(), e.getScoreDate()));
        sb.append(String.format("Balance Owed: $%s%n", e.getCurrentBalance()));
        sb.append(String.format("Reach Score: %.1f / 100   Recovery Score: %.1f / 100%n%n",
                e.getReachScore(), e.getRecoveryScore()));
        sb.append("--- Key Facts ---\n");
        sb.append(String.format("- Previous loans: %d%n", e.getPreviousLoansCount()));
        sb.append(String.format("- PTPs made: %d | Broken PTP: %s%n",
                e.getPtpMadeCount(), e.isHasBrokenPtp() ? "YES" : "No"));
        sb.append(String.format("- App installed: %s%n", e.isAppInstalled() ? "Yes" : "No"));
        sb.append(String.format("- Days since last contact: %d%n", e.getDaysSinceLastContact()));
        sb.append("\n--- Strategy Tips ---\n");
        if (e.isHasBrokenPtp()) {
            sb.append("- Client has broken PTP. Ask for smaller, achievable amounts.\n");
        }
        if (e.isAppInstalled()) {
            sb.append("- App installed. Offer self-service payment through the app.\n");
        }
        if (e.getDaysSinceLastContact() > 30) {
            sb.append("- Long gap since contact. Re-establish rapport first.\n");
        }
        if (e.getRecoveryScore() > 60) {
            sb.append("- High recovery potential. Push for agreement today.\n");
        }
        return sb.toString();
    }
}
