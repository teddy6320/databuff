package com.databuff.apm.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiApmIngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApmIngestApplication.class, args);
    }
}
