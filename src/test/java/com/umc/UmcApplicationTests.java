package com.umc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // ← 이거 꼭 추가!
class UmcApplicationTests {

    @Test
    void contextLoads() {
    }

} 