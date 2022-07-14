package com.debug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DebugWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebugWebApplication.class, args);
    }

}
