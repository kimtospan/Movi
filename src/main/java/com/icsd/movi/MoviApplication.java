package com.icsd.movi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoviApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoviApplication.class, args);
    }

}
