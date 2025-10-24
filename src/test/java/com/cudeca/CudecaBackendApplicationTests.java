package com.cudeca;

import com.cudeca.config.MailConfigTest;
import com.cudeca.testutil.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(MailConfigTest.class)
@IntegrationTest
class CudecaBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodRuns() {
        CudecaBackendApplication.main(new String[]{});
    }
}