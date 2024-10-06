package com.oket.Integration_of_paypal.paypal.services;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;

@Service // Marks this class as a Spring Service, which is a specialization of a component for business logic.
@RequiredArgsConstructor // Lombok annotation to generate a constructor with all final fields (i.e., dependency injection).
public class PaypalServiceImpl implements PaypalService {

    // APIContext is used to configure PayPal API access, including credentials and configuration settings.
    private final APIContext apiContext;

    @Override
    // Creates a PayPal payment and returns the approval URL for redirecting the user to PayPal for approval.
    public String createPaymentWithApprovalUrl(Double total, String currency, String method, String intent,
                                               String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {
        // Calls helper method to create the payment object with transaction details.
        Payment payment = createPayment(total, currency, method, intent, description, cancelUrl, successUrl);
        // Extracts the approval URL from the payment response.
        return extractApprovalUrl(payment);
    }

    @Override
    // Executes the PayPal payment after approval and checks if the payment state is "approved".
    public boolean executePaymentAndCheckState(String paymentId, String payerId) throws PayPalRESTException {
        // Executes the payment using paymentId and payerId received after user approval.
        Payment payment = executePayment(paymentId, payerId);
        // Returns true if the payment state is "approved", false otherwise.
        return "approved".equals(payment.getState());
    }

    // Helper method to create a Payment object, which includes transaction details, payer, and redirect URLs.
    public Payment createPayment(Double total, String currency, String method, String intent,
                                 String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {
        // Builds a transaction with the provided description and amount.
        Transaction transaction = buildTransaction(description, buildAmount(total, currency));
        // Returns a Payment object with intent, payer, transaction, and redirect URLs.
        return new Payment()
                .setIntent(intent) // Sets payment intent (e.g., "sale").
                .setPayer(buildPayer(method)) // Sets payer details (e.g., payment method like "paypal").
                .setTransactions(Collections.singletonList(transaction)) // Adds the transaction to the payment.
                .setRedirectUrls(buildRedirectUrls(cancelUrl, successUrl)) // Sets redirect URLs for success and cancellation.
                .create(apiContext); // Calls PayPal API to create the payment using API context.
    }

    // Helper method to execute a payment after PayPal approval using paymentId and payerId.
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        // Creates a PaymentExecution object with payerId, required for executing the payment.
        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(payerId);
        // Executes the payment with the given paymentId and execution context.
        return new Payment().setId(paymentId).execute(apiContext, paymentExecution);
    }

    // Extracts the approval URL from the PayPal payment response, which the user is redirected to for approval.
    private String extractApprovalUrl(Payment payment) throws PayPalRESTException {
        // Streams through the payment's links to find the approval link (URL).
        return payment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel())) // Filters the link with relation "approval_url".
                .map(Links::getHref) // Maps the link to its href (URL).
                .findFirst() // Finds the first matching link.
                .orElseThrow(() -> new PayPalRESTException("Approval URL not found")); // Throws an exception if not found.
    }

    // Helper method to build an Amount object representing the payment amount and currency.
    private Amount buildAmount(Double total, String currency) {
        // Creates and returns an Amount object with currency and total amount formatted to 2 decimal places.
        return new Amount()
                .setCurrency(currency) // Sets the currency (e.g., "USD").
                .setTotal(String.format(Locale.US, "%.2f", total)); // Sets the total amount formatted as a string.
    }

    // Helper method to build a Transaction object representing the payment transaction.
    private Transaction buildTransaction(String description, Amount amount) {
        // Creates and returns a Transaction object with a description and amount.
        return (Transaction) new Transaction()
                .setDescription(description) // Sets the transaction description (e.g., "Payment for services").
                .setAmount(amount); // Sets the amount for the transaction.
    }

    // Helper method to build a Payer object representing the payer's details (e.g., payment method).
    private Payer buildPayer(String method) {
        // Creates and returns a Payer object with the specified payment method (e.g., "paypal").
        return new Payer().setPaymentMethod(method);
    }

    // Helper method to build a RedirectUrls object representing the URLs for success and cancellation.
    private RedirectUrls buildRedirectUrls(String cancelUrl, String successUrl) {
        // Creates and returns a RedirectUrls object with cancel and success URLs.
        return new RedirectUrls()
                .setCancelUrl(cancelUrl) // Sets the URL to redirect to in case of payment cancellation.
                .setReturnUrl(successUrl); // Sets the URL to redirect to upon successful payment.
    }
}

