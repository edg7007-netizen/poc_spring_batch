package com.example.phonebatch.dialer;

import com.example.phonebatch.domain.ScoredPhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("awsConnectDialerStrategy")
public class AwsConnectDialerStrategy implements DialerStrategy {

    private static final Logger log = LoggerFactory.getLogger(AwsConnectDialerStrategy.class);

    @Override
    public void sendPhoneList(List<ScoredPhoneNumber> phoneNumbers) {
        log.info("[AWS Connect] Sending {} phone numbers to AWS Connect", phoneNumbers.size());
        phoneNumbers.forEach(p -> log.info("[AWS Connect] Phone: {}, ReachScore: {}, RecoveryScore: {}",
            p.getRawData().getPhoneNumber(), p.getReachScore(), p.getRecoveryScore()));
    }
}
