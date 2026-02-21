package com.jumbotail.shipping.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRateLimiting() throws Exception {
        String testIp = "192.168.1.100";

        // The current limit is 100 requests per minute
        // We will send 100 successful requests
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/actuator/health")
                    .header("X-Forwarded-For", testIp))
                    .andExpect(status().isOk());
        }

        // The 101st request should be rejected with 429 Too Many Requests
        mockMvc.perform(get("/actuator/health")
                .header("X-Forwarded-For", testIp))
                .andExpect(status().isTooManyRequests());
        
        // However, a different IP should still be allowed
        String differentIp = "192.168.1.101";
        mockMvc.perform(get("/actuator/health")
                .header("X-Forwarded-For", differentIp))
                .andExpect(status().isOk());
    }
}
