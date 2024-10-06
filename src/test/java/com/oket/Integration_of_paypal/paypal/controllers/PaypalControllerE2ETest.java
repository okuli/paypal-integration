package com.oket.Integration_of_paypal.paypal.controllers;

import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PaypalControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaypalService paypalService;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Test
    public void testCreatePayment_Success() throws Exception {
        when(paypalService.createPaymentWithApprovalUrl(anyDouble(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString())).thenReturn("http://mockapproval.com");

        mockMvc.perform(post("/payment/create")
                        .header("Accept", MediaType.TEXT_HTML_VALUE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://mockapproval.com"));
    }

    @Test
    public void testPaymentSuccess_Approved() throws Exception {
        when(paypalService.executePaymentAndCheckState(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/payment/success")
                        .param("paymentId", "mockPaymentId")
                        .param("PayerID", "mockPayerId")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.paymentId").value("mockPaymentId"))
                .andExpect(jsonPath("$.payerId").value("mockPayerId"));
    }

    @Test
    public void testPaymentCancel() throws Exception {
        mockMvc.perform(get("/payment/cancel")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"))
                .andExpect(jsonPath("$.message").value("Payment cancelled by user"));
    }

    @Test
    public void testPaymentError() throws Exception {
        mockMvc.perform(get("/payment/error")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("An error occurred during the payment process"));
    }
}



