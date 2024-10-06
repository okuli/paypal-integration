package com.oket.Integration_of_paypal.paypal.controllers;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.oket.Integration_of_paypal.paypal.services.PaypalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PaypalServiceUnitTest {

    @InjectMocks
    private PaypalServiceImpl paypalService;  // Use the concrete implementation

    @Mock
    private APIContext apiContext;  // Mock the PayPal API context

    @Mock
    private Payment payment;  // Mock the Payment object

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks before each test
    }

    @Test
    public void testExecutePaymentAndCheckState_Approved() throws PayPalRESTException {
        // Given: Payment ID and Payer ID
        String paymentId = "PAY-123456";
        String payerId = "PAYER-123456";

        // Mock PaymentExecution and API interaction
        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(payerId);
        when(payment.execute(apiContext, paymentExecution)).thenReturn(payment);
        when(payment.getState()).thenReturn("approved");

        // Spy on the service to mock the executePayment method
        PaypalServiceImpl paypalServiceSpy = spy(paypalService);
        doReturn(payment).when(paypalServiceSpy).executePayment(paymentId, payerId);

        // When: Calling the method under test
        boolean result = paypalServiceSpy.executePaymentAndCheckState(paymentId, payerId);

        // Then: Assert the result is true as the payment state is "approved"
        assertTrue(result);

        // Verify: Ensure executePayment was called with the correct parameters
        verify(paypalServiceSpy).executePayment(paymentId, payerId);
    }
}

