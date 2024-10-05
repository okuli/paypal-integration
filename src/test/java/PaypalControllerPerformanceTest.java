import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.oket.Integration_of_paypal.IntegrationOfPaypalApplication.class)
@RequiredArgsConstructor
public class PaypalControllerPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PaypalControllerPerformanceTest.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private PaypalService paypalService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @Operation(summary = "Test create payment performance")
    public void testCreatePaymentPerformance() throws Exception {
        // Mocking the service method
        when(paypalService.createPaymentWithApprovalUrl(anyDouble(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString())).thenReturn("http://approval.url");

        long startTime = System.currentTimeMillis();

        // Performing the POST request and verifying the response
        mockMvc.perform(post("/payment/create")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalUrl").value("http://approval.url"));

        log.info("Performance Test Duration: {} ms", System.currentTimeMillis() - startTime);
    }
}
