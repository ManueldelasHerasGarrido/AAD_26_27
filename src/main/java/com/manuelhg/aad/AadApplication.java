package com.manuelhg.aad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AadApplication {

    public static void main(String[] args) {
        SpringApplication.run(AadApplication.class, args);
    }

}
