package com.icsd.movi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MoviApplicationTests {

    @Test
    void contextLoads() {
        // Happy path: Application context load test. Verifies that the Spring Boot application starts correctly with the test configuration.
    }

}
