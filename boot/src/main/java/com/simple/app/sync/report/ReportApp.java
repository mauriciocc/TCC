package com.simple.app.sync.report;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ReportApp {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
