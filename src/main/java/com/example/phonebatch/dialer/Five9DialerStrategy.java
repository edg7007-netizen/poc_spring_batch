package com.example.phonebatch.dialer;

import com.example.phonebatch.domain.ScoredPhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("five9DialerStrategy")
public class Five9DialerStrategy implements DialerStrategy {

    private static final Logger log = LoggerFactory.getLogger(Five9DialerStrategy.class);

    @Override
    public void sendPhoneList(List<ScoredPhoneNumber> phoneNumbers) {
        log.info("[Five9] Sending {} phone numbers to Five9 dialer", phoneNumbers.size());
        phoneNumbers.forEach(p -> log.info("[Five9] Phone: {}, ReachScore: {}, RecoveryScore: {}",
            p.getRawData().getPhoneNumber(),
            String.format("%.4f", p.getReachScore()),
            String.format("%.4f", p.getRecoveryScore())));
    }
}
