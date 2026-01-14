package com.recomon;

import com.recomon.config.TestAIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAIConfig.class)
class RecomonApplicationTests {

    @Test
    void contextLoads() {
    }

}
