package com.oket.Integration_of_paypal.services;

import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.api.payments.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaypalServiceUnitTest {

    @InjectMocks
    private PaypalService paypalService;

    @Mock
    private APIContext apiContext; // Assuming APIContext is correctly mocked here

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExecutePaymentAndCheckState_Approved() throws PayPalRESTException {
        String paymentId = "valid_payment_id"; // Mock a valid payment ID
        String payerId = "valid_payer_id"; // Mock a valid payer ID

        // Mock the behavior for APIContext and Payment
        Payment mockedPayment = mock(Payment.class);
        when(mockedPayment.getState()).thenReturn("approved");
        when(apiContext.getAccessToken()).thenReturn("mock_access_token"); // Ensure you mock access token
        when(paypalService.executePayment(paymentId, payerId)).thenReturn(mockedPayment);

        // Call the method under test
        boolean result = paypalService.executePaymentAndCheckState(paymentId, payerId);

        // Assert that the result indicates the payment was approved
        assertTrue(result);
    }


}
