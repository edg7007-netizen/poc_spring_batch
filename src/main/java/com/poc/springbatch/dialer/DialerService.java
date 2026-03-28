package com.poc.springbatch.dialer;

import com.poc.springbatch.model.PhoneScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Facade service that sorts the phone score list and delegates to the configured
 * {@link DialerStrategy} implementation.
 *
 * To switch diallers, change the {@code app.dialer.provider} property.
 * No code changes are required thanks to the Strategy pattern.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialerService {

    private final DialerStrategy dialerStrategy;

    /**
     * Sorts phone scores descending by reach score and sends them to the dialler.
     *
     * @param scores unsorted list of computed phone scores.
     */
    public void dispatch(List<PhoneScore> scores) {
        List<PhoneScore> sorted = scores.stream()
                .sorted(Comparator.comparingDouble(PhoneScore::getReachScore).reversed())
                .toList();

        log.info("DialerService: dispatching {} numbers to {} (sorted by reach score desc)",
                sorted.size(), dialerStrategy.getDialerName());

        dialerStrategy.sendPhoneList(sorted);
    }
}
