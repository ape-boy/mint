package com.mintportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MintPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MintPortalApplication.class, args);
    }
}
