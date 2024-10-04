package com.oket.Integration_of_paypal.services;

import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


public class PaypalServiceUnitTest {

    @InjectMocks
    private PaypalService paypalService;

    @Mock
    private APIContext apiContext;

    @BeforeEach
    public void setUp() throws PayPalRESTException {
        MockitoAnnotations.openMocks(this);

        // Mock APIContext to return a valid access token
        when(apiContext.fetchAccessToken()).thenReturn("mock_access_token");
    }

    @Test
    public void testExecutePaymentAndCheckState_Approved() throws PayPalRESTException {
        // Create a spy for the real PaypalService
        PaypalService paypalServiceSpy = Mockito.spy(paypalService);

        // Mock valid payment and payer IDs
        String paymentId = "valid_payment_id";
        String payerId = "valid_payer_id";

        // Mock Payment behavior
        Payment mockedPayment = mock(Payment.class);
        when(mockedPayment.getState()).thenReturn("approved");

        // Mock the execution of the payment (on spy)
        doReturn(mockedPayment).when(paypalServiceSpy).executePayment(paymentId, payerId);

        // Call the method under test
        boolean result = paypalServiceSpy.executePaymentAndCheckState(paymentId, payerId);

        // Assert the result indicates the payment was approved
        assertTrue(result);

        // Verify that executePayment was called once
        verify(paypalServiceSpy, times(1)).executePayment(paymentId, payerId);
    }
}