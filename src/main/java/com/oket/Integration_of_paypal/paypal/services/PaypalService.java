package com.oket.Integration_of_paypal.paypal.services;

import com.paypal.base.rest.PayPalRESTException;

// Defines the PaypalService interface which contains methods to interact with PayPal API.
public interface PaypalService {

    /**
     * Creates a PayPal payment and returns the approval URL for user redirection.
     *
     * @param total       the total payment amount
     * @param currency    the currency type (e.g., USD)
     * @param method      the payment method (e.g., "paypal")
     * @param intent      the payment intent (e.g., "sale")
     * @param description a description of the payment
     * @param cancelUrl   the URL to redirect if the payment is cancelled
     * @param successUrl  the URL to redirect upon successful payment
     * @return the approval URL where the user should be redirected to approve the payment
     * @throws PayPalRESTException if an error occurs during payment creation
     */
    String createPaymentWithApprovalUrl(Double total, String currency, String method, String intent,
                                        String description, String cancelUrl, String successUrl)
            throws PayPalRESTException;

    /**
     * Executes a PayPal payment after approval and checks if the payment was successfully approved.
     *
     * @param paymentId the ID of the payment returned by PayPal
     * @param payerId   the ID of the payer returned by PayPal
     * @return true if the payment is approved, false otherwise
     * @throws PayPalRESTException if an error occurs during payment execution
     */
    boolean executePaymentAndCheckState(String paymentId, String payerId) throws PayPalRESTException;
}

