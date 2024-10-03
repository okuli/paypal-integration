package com.oket.Integration_of_paypal.paypal.controllers;

import com.oket.Integration_of_paypal.paypal.controllers.PaypalController;
import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.base.rest.PayPalRESTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        // Mock the service to return a valid approval URL
        when(paypalService.createPaymentWithApprovalUrl(anyDouble(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString())).thenReturn("http://approval.url");

        // Perform the request and check for success
        mockMvc.perform(post("/payment/create")
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.approvalUrl").value("http://approval.url"));
    }
}
