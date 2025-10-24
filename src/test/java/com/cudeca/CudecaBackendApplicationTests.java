package com.cudeca;

import com.cudeca.config.MailConfigTest;
import com.cudeca.testutil.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Import(MailConfigTest.class)
@IntegrationTest
class CudecaBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodRuns() {
        assertDoesNotThrow(() ->
                SpringApplication
                        .from(CudecaBackendApplication::main)
                        .with(MailConfigTest.class)
                        .run("--spring.profiles.active=test")
        );
    }
}
