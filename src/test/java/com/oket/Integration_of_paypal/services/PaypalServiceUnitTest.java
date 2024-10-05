//package com.oket.Integration_of_paypal.services;
//
//import com.oket.Integration_of_paypal.paypal.services.PaypalService;
//import com.paypal.api.payments.Payment;
//import com.paypal.base.rest.APIContext;
//import com.paypal.base.rest.PayPalRESTException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.*;
//
//public class PaypalServiceUnitTest {
//
//    @InjectMocks
//    private PaypalService paypalService;
//
//    @Mock
//    private APIContext apiContext;
//
//    @BeforeEach
//    public void setUp() throws PayPalRESTException {
//        MockitoAnnotations.openMocks(this);
//        when(apiContext.fetchAccessToken()).thenReturn("mock_access_token");
//    }
//
//    @Test
//    public void testExecutePaymentAndCheckState_Approved() throws PayPalRESTException {
//        // Define payment and payer IDs
//        String paymentId = "valid_payment_id";
//        String payerId = "valid_payer_id";
//
//        // Mock Payment behavior
//        Payment mockedPayment = mock(Payment.class);
//        when(mockedPayment.getState()).thenReturn("approved");
//
//        // Spy on the PaypalService and mock executePayment method
//        PaypalService paypalServiceSpy = Mockito.spy(paypalService);
//        doReturn(mockedPayment).when(paypalServiceSpy).executePayment(paymentId, payerId);
//
//        // Call the method under test and assert the result
//        boolean result = paypalServiceSpy.executePaymentAndCheckState(paymentId, payerId);
//        assertTrue(result);
//
//        // Verify executePayment was called once
//        verify(paypalServiceSpy).executePayment(paymentId, payerId);
//    }
//}
