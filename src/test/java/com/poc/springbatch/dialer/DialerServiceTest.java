package com.poc.springbatch.dialer;

import com.poc.springbatch.model.PhoneScore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class DialerServiceTest {

    @Test
    void dispatch_sortsByReachScoreDescending() {
        DialerStrategy strategy = mock(DialerStrategy.class);
        when(strategy.getDialerName()).thenReturn("MockDialer");

        DialerService service = new DialerService(strategy);

        PhoneScore low    = PhoneScore.builder().phoneNumber("111").reachScore(20.0).build();
        PhoneScore medium = PhoneScore.builder().phoneNumber("222").reachScore(55.0).build();
        PhoneScore high   = PhoneScore.builder().phoneNumber("333").reachScore(85.0).build();

        service.dispatch(Arrays.asList(low, high, medium));

        var captor = forClass(List.class);
        verify(strategy).sendPhoneList(captor.capture());

        @SuppressWarnings("unchecked")
        List<PhoneScore> sent = (List<PhoneScore>) captor.getValue();

        assertThat(sent).hasSize(3);
        assertThat(sent.get(0).getPhoneNumber()).isEqualTo("333");
        assertThat(sent.get(1).getPhoneNumber()).isEqualTo("222");
        assertThat(sent.get(2).getPhoneNumber()).isEqualTo("111");
    }

    @Test
    void dispatch_withEmptyList_doesNotThrow() {
        DialerStrategy strategy = mock(DialerStrategy.class);
        when(strategy.getDialerName()).thenReturn("MockDialer");

        DialerService service = new DialerService(strategy);
        service.dispatch(List.of());

        verify(strategy).sendPhoneList(List.of());
    }
}
