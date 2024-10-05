package com.oket.Integration_of_paypal.paypal.services;

public interface VippsService {
    String createPayment(Double total, String currency, String description, String orderId) throws Exception;
    boolean executePayment(String orderId, String transactionId) throws Exception;
}

