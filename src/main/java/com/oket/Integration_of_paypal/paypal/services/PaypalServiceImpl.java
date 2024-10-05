package com.oket.Integration_of_paypal.paypal.services;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaypalServiceImpl implements PaypalService {

    private final APIContext apiContext;

    @Override
    public String createPaymentWithApprovalUrl(Double total, String currency, String method, String intent,
                                               String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {
        Payment payment = createPayment(total, currency, method, intent, description, cancelUrl, successUrl);
        return extractApprovalUrl(payment);
    }

    @Override
    public boolean executePaymentAndCheckState(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = executePayment(paymentId, payerId);
        return "approved".equals(payment.getState());
    }

    public Payment createPayment(Double total, String currency, String method, String intent,
                                 String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {
        Transaction transaction = buildTransaction(description, buildAmount(total, currency));
        return new Payment()
                .setIntent(intent)
                .setPayer(buildPayer(method))
                .setTransactions(Collections.singletonList(transaction))
                .setRedirectUrls(buildRedirectUrls(cancelUrl, successUrl))
                .create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(payerId);
        return new Payment().setId(paymentId).execute(apiContext, paymentExecution);
    }

    private String extractApprovalUrl(Payment payment) throws PayPalRESTException {
        return payment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .map(Links::getHref)
                .findFirst()
                .orElseThrow(() -> new PayPalRESTException("Approval URL not found"));
    }

    private Amount buildAmount(Double total, String currency) {
        return new Amount()
                .setCurrency(currency)
                .setTotal(String.format(Locale.US, "%.2f", total));
    }

    private Transaction buildTransaction(String description, Amount amount) {
        return (Transaction) new Transaction()
                .setDescription(description)
                .setAmount(amount);
    }

    private Payer buildPayer(String method) {
        return new Payer().setPaymentMethod(method);
    }

    private RedirectUrls buildRedirectUrls(String cancelUrl, String successUrl) {
        return new RedirectUrls()
                .setCancelUrl(cancelUrl)
                .setReturnUrl(successUrl);
    }
}
