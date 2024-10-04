package com.oket.Integration_of_paypal.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.api.payments.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PaypalServiceUnitTest {

//    @InjectMocks
//    private PaypalService paypalService;
//
//    @Mock
//    private APIContext apiContext;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testExecutePaymentAndCheckState_Approved() throws PayPalRESTException {
//        // Mock valid payment and payer IDs
//        String paymentId = "valid_payment_id";
//        String payerId = "valid_payer_id";
//
//        // Mock Payment behavior
//        Payment mockedPayment = mock(Payment.class);
//        when(mockedPayment.getState()).thenReturn("approved");
//        when(mockedPayment.execute(any(APIContext.class), anyString())).thenReturn(mockedPayment);
//
//        // Mock APIContext behavior
//        when(apiContext.getAccessToken()).thenReturn("mock_access_token");
//        when(paypalService.executePayment(paymentId, payerId)).thenReturn(mockedPayment);
//
//        // Call the method under test
//        boolean result = paypalService.executePaymentAndCheckState(paymentId, payerId);
//
//        // Assert the result indicates the payment was approved
//        assertTrue(result);
//    }
}


