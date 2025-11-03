package com.example.ecoms.Config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        // FULL = shows request + response headers, body, and metadata
        return Logger.Level.FULL;
    }
}
