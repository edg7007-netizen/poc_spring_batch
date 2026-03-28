package com.example.phonebatch.dialer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DialerConfig {

    @Value("${app.dialer.provider:five9}")
    private String dialerProvider;

    @Bean
    @Primary
    public DialerStrategy dialerStrategy(
            @Qualifier("five9DialerStrategy") DialerStrategy five9,
            @Qualifier("awsConnectDialerStrategy") DialerStrategy awsConnect) {
        return "aws-connect".equals(dialerProvider) ? awsConnect : five9;
    }
}
