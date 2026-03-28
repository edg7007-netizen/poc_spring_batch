package com.example.phonebatch.dialer;

import com.example.phonebatch.domain.PhoneNumberRawData;
import com.example.phonebatch.domain.ScoredPhoneNumber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class DialerStrategyTest {

    @Autowired
    private DialerStrategy dialerStrategy;

    @Autowired
    private Five9DialerStrategy five9DialerStrategy;

    @Test
    void testDefaultDialerIsFive9() {
        assertThat(dialerStrategy).isInstanceOf(Five9DialerStrategy.class);
    }

    @Test
    void testFive9SendsPhoneList() {
        PhoneNumberRawData raw = new PhoneNumberRawData("555-0001");
        raw.setPreviousLoans(3);
        raw.setHistoricalAnswerRate(0.7);
        ScoredPhoneNumber scored = new ScoredPhoneNumber(raw, 0.65, 0.55);

        assertThatCode(() -> five9DialerStrategy.sendPhoneList(List.of(scored)))
            .doesNotThrowAnyException();
    }
}
