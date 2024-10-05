//package com.oket.Integration_of_paypal.paypal.controllers;
//
//import com.oket.Integration_of_paypal.paypal.services.PaypalService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//public class PaypalControllerIntegrationTest {
//
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    @MockBean
//    private PaypalService paypalService;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    public void setup() {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }
//
//    @Test
//    public void testCreatePayment() throws Exception {
//        String expectedApprovalUrl = "http://approval.url";
//
//        when(paypalService.createPaymentWithApprovalUrl(anyDouble(), anyString(), anyString(), anyString(),
//                anyString(), anyString(), anyString())).thenReturn(expectedApprovalUrl);
//
//        mockMvc.perform(post("/payment/create")
//                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.approvalUrl").value(expectedApprovalUrl));
//    }
//
//    @Test
//    public void testPaymentSuccess() throws Exception {
//        String paymentId = "PAYID";
//        String payerId = "PAYERID";
//
//        when(paypalService.executePaymentAndCheckState(anyString(), anyString())).thenReturn(true);
//
//        mockMvc.perform(get("/payment/success")
//                        .param("paymentId", paymentId)
//                        .param("PayerID", payerId)
//                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.paymentId").value(paymentId));
//    }
//}
//
