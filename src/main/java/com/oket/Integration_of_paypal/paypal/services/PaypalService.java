package com.oket.Integration_of_paypal.paypal.services;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Locale;

public interface PaypalService {
    String createPaymentWithApprovalUrl(Double total, String currency, String method, String intent,
                                        String description, String cancelUrl, String successUrl)
            throws PayPalRESTException;

    boolean executePaymentAndCheckState(String paymentId, String payerId) throws PayPalRESTException;
}

