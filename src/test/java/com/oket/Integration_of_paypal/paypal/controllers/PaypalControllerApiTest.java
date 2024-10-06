package com.oket.Integration_of_paypal.paypal.controllers;


import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@SpringBootTest
public class PaypalControllerApiTest {

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
    public void testCreatePayment() throws Exception {
        String expectedApprovalUrl = "http://approval.url";

        when(paypalService.createPaymentWithApprovalUrl(
                anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedApprovalUrl);

        // Perform the request and check for success
        mockMvc.perform(post("/payment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalUrl").value(expectedApprovalUrl));
    }
}
