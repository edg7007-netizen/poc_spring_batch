package com.example.phonebatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhoneBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhoneBatchApplication.class, args);
    }
}
