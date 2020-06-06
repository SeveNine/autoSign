package com.sevenine.autosign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutosignApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutosignApplication.class, args);
    }

}
