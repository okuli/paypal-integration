package com.oket.Integration_of_paypal.paypal.services;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class VippsServiceImpl implements VippsService {
//
//    @Value("${vipps.client.id}")
//    private String clientId;
//
//    @Value("${vipps.client.secret}")
//    private String clientSecret;
//
//    @Value("${vipps.api.url}")
//    private String vippsApiUrl;
//
//    private final RestTemplate restTemplate; // Assume RestTemplate is properly configured
//
//    public VippsServiceImpl(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    @Override
//    public String createPayment(Double total, String currency, String description, String orderId) throws Exception {
//        // Construct request payload for VIPPS payment
//        // Make an HTTP POST request to VIPPS API to create payment
//        String apiUrl = vippsApiUrl + "/payment"; // Replace with actual VIPPS endpoint
//
//        // Construct the request body
//        PaymentRequest paymentRequest = new PaymentRequest(orderId, total, currency, description);
//        // Add more details to paymentRequest as needed
//
//        // Call the VIPPS API
//        PaymentResponse response = restTemplate.postForObject(apiUrl, paymentRequest, PaymentResponse.class);
//
//        if (response == null || !response.isSuccessful()) {
//            throw new Exception("Failed to create payment");
//        }
//
//        return response.getApprovalUrl(); // Or however VIPPS responds with the approval URL
//    }
//
//    @Override
//    public boolean executePayment(String orderId, String transactionId) throws Exception {
//        // Call VIPPS API to execute payment
//        String apiUrl = vippsApiUrl + "/payment/execute"; // Replace with actual VIPPS endpoint
//        ExecutePaymentRequest executeRequest = new ExecutePaymentRequest(orderId, transactionId);
//
//        PaymentExecutionResponse response = restTemplate.postForObject(apiUrl, executeRequest, PaymentExecutionResponse.class);
//
//        return response != null && response.isApproved();
//    }
//}
//
//
//
