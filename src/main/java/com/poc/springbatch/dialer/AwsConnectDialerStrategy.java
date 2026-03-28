package com.poc.springbatch.dialer;

import com.poc.springbatch.model.PhoneScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AWS Connect dialler strategy.
 *
 * Mocked for demonstration – replace the body of {@code sendPhoneList} with
 * the actual AWS Connect SDK calls once the instance and queue are configured.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.dialer.provider", havingValue = "aws-connect")
public class AwsConnectDialerStrategy implements DialerStrategy {

    @Override
    public String getDialerName() {
        return "AWS Connect";
    }

    @Override
    public void sendPhoneList(List<PhoneScore> sortedScores) {
        log.info("[AWS Connect] Sending {} phone numbers to dialler (sorted by reach score desc)",
                sortedScores.size());

        for (int i = 0; i < sortedScores.size(); i++) {
            PhoneScore s = sortedScores.get(i);
            log.info("[AWS Connect] #{} phone={} reachScore={} recoveryScore={}",
                    i + 1, s.getPhoneNumber(),
                    String.format("%.2f", s.getReachScore()),
                    String.format("%.2f", s.getRecoveryScore()));
        }

        /*
         * Production implementation would:
         *   1. Use software.amazon.awssdk:connect SDK
         *   2. Call ConnectClient.startOutboundVoiceContact() for each entry, or
         *      batch-import via a Contact Flows campaign
         *   3. Handle ContactFlowId, QueueId, InstanceId from config
         *   4. Respect the Connect service quotas (outbound contacts/sec)
         */
        log.info("[AWS Connect] Phone list submitted successfully (mock)");
    }
}
