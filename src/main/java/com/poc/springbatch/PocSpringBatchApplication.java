package com.poc.springbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PocSpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocSpringBatchApplication.class, args);
    }
}
