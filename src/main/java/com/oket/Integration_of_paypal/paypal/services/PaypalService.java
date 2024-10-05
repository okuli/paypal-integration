package com.oket.Integration_of_paypal.paypal.services;

import com.paypal.base.rest.PayPalRESTException;

public interface PaypalService {
    String createPaymentWithApprovalUrl(Double total, String currency, String method, String intent,
                                        String description, String cancelUrl, String successUrl)
            throws PayPalRESTException;

    boolean executePaymentAndCheckState(String paymentId, String payerId) throws PayPalRESTException;
}

