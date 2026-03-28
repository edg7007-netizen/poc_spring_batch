package com.poc.springbatch.dialer;

import com.poc.springbatch.model.PhoneScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Five9 dialler strategy.
 *
 * Mocked for demonstration – replace the body of {@code sendPhoneList} with
 * the actual Five9 REST API call once credentials/endpoint are configured.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.dialer.provider", havingValue = "five9", matchIfMissing = true)
public class Five9DialerStrategy implements DialerStrategy {

    @Override
    public String getDialerName() {
        return "Five9";
    }

    @Override
    public void sendPhoneList(List<PhoneScore> sortedScores) {
        log.info("[Five9] Sending {} phone numbers to dialler (sorted by reach score desc)",
                sortedScores.size());

        // --- Mock: log the ordered list ---
        for (int i = 0; i < sortedScores.size(); i++) {
            PhoneScore s = sortedScores.get(i);
            log.info("[Five9] #{} phone={} reachScore={} recoveryScore={}",
                    i + 1, s.getPhoneNumber(),
                    String.format("%.2f", s.getReachScore()),
                    String.format("%.2f", s.getRecoveryScore()));
        }

        /*
         * Production implementation would:
         *   1. Build a Five9 campaign list payload
         *   2. POST to Five9 REST API endpoint:
         *      POST https://<tenant>.five9.com/api/2/subscribers/import
         *   3. Handle authentication (OAuth / API key)
         *   4. Handle rate limiting and error responses
         */
        log.info("[Five9] Phone list submitted successfully (mock)");
    }
}
