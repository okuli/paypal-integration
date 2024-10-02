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
public class PaypalService {

    private final APIContext apiContext;

    public String createPaymentWithApprovalUrl(Double total, String currency, String method, String intent,
                                               String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {

        Payment payment = createPayment(total, currency, method, intent, description, cancelUrl, successUrl);

        return payment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .map(Links::getHref)
                .findFirst()
                .orElseThrow(() -> new PayPalRESTException("Approval URL not found"));
    }

    public boolean executePaymentAndCheckState(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = executePayment(paymentId, payerId);
        return "approved".equals(payment.getState());
    }

    private Payment createPayment(Double total, String currency, String method, String intent,
                                  String description, String cancelUrl, String successUrl) throws PayPalRESTException {

        Amount amount = createAmount(total, currency);
        Transaction transaction = createTransaction(description, amount);
        Payer payer = createPayer(method);
        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(Collections.singletonList(transaction));
        payment.setRedirectUrls(createRedirectUrls(cancelUrl, successUrl));
        return payment.create(apiContext);
    }

    private Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecution);
    }

    // Utility methods

    private Amount createAmount(Double total, String currency) {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total));
        return amount;
    }

    private Transaction createTransaction(String description, Amount amount) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        return transaction;
    }

    private Payer createPayer(String method) {
        Payer payer = new Payer();
        payer.setPaymentMethod(method);
        return payer;
    }

    private RedirectUrls createRedirectUrls(String cancelUrl, String successUrl) {
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        return redirectUrls;
    }
}
