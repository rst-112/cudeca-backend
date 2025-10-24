package com.cudeca;

import com.cudeca.config.MailConfigTest;
import com.cudeca.testutil.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(MailConfigTest.class)
@IntegrationTest
class CudecaBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodRuns() {
        System.setProperty("spring.profiles.active", "test");

        try (ConfigurableApplicationContext ctx =
                     SpringApplication.run(CudecaBackendApplication.class)) {
            assert ctx.isActive();
        }
    }
}
