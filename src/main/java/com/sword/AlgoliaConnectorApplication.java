package com.sword;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.Security;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AlgoliaConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgoliaConnectorApplication.class, args);
        Security.setProperty("networkaddress.cache.ttl", "60");
    }

}