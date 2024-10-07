package com.oket.Integration_of_paypal.paypal.controllers;
import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.annotation.PostConstruct;


@Tag(name = "Paypal", description = "The Paypal API")
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaypalController {
    private final PaypalService paypalService;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    private static final String INDEX = "index";
    private static final String SUCCESS = "success";
    private static final String PAYMENT_SUCCESS = "paymentSuccess";
    private static final String PAYMENT_CREATION_FAILED = "Payment creation failed";
    private static final String PAYMENT_EXECUTION_FAILED = "Payment execution failed";

    // Eager configuration validation
    @PostConstruct
    private void validateConfigUrls() {
        if (successUrl == null || successUrl.isBlank()) {
            throw new IllegalArgumentException("PayPal success URL is missing or empty");
        }
        if (cancelUrl == null || cancelUrl.isBlank()) {
            throw new IllegalArgumentException("PayPal cancel URL is missing or empty");
        }
        log.info("PayPal configuration URLs validated successfully: Success URL - {}, Cancel URL - {}", successUrl, cancelUrl);
    }

    @GetMapping("/")
    public String home() {
        return INDEX;
    }

    public record PaymentResponse(String status, String paymentId, String payerId) {}
    public record ErrorResponse(String status, String message, String error) {}
    public record CancelResponse(String status, String message) {}
    public record SuccessResponse(String status, String approvalUrl) {}

    @Operation(summary = "Create a new PayPal payment",
            description = "Creates a new PayPal payment and returns either a JSON response or redirects to a payment approval URL.")
    @PostMapping(value = "/payment/create", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE})
    public Object createPayment(@RequestHeader(value = "Accept", required = false) String accept) {
        try {
            String approvalUrl = createPayPalPayment();
            return respondBasedOnAcceptHeader(accept, approvalUrl);
        } catch (PayPalRESTException e) {
            log.error("Error occurred while creating payment. Accept: {}, Error: {}", accept, e.getMessage(), e);
            return handleError(accept, PAYMENT_CREATION_FAILED, "/payment/error", e);
        }
    }

    @Operation(summary = "Handle successful payment",
            description = "Handles a successful payment and returns a success view or JSON response.")
    @GetMapping("/payment/success")
    public Object paymentSuccess(@RequestHeader(value = "Accept", required = false) String accept,
                                 @RequestParam("paymentId") String paymentId,
                                 @RequestParam("PayerID") String payerId) {
        validatePaymentParams(paymentId, payerId);

        try {
            if (executePayment(paymentId, payerId)) {
                return createResponse(accept, new PaymentResponse(SUCCESS, paymentId, payerId), PAYMENT_SUCCESS);
            }
        } catch (PayPalRESTException e) {
            log.error("Error during payment execution. PaymentId: {}, PayerId: {}, Error: {}", paymentId, payerId, e.getMessage(), e);
        }
        return handleError(accept, PAYMENT_EXECUTION_FAILED, PAYMENT_SUCCESS, null);
    }

    private String createPayPalPayment() throws PayPalRESTException {
        return paypalService.createPaymentWithApprovalUrl(
                10.0, "USD", "paypal", "sale", "Payment description", cancelUrl, successUrl);
    }

    private boolean executePayment(String paymentId, String payerId) throws PayPalRESTException {
        return paypalService.executePaymentAndCheckState(paymentId, payerId);
    }

    @Operation(summary = "Handle payment cancellation",
            description = "Handles a cancelled payment and returns either a cancellation view or a JSON response.")
    @GetMapping("/payment/cancel")
    public Object paymentCancel(@RequestHeader(value = "Accept", required = false) String accept) {
        return createResponse(accept, new CancelResponse("cancelled", "Payment cancelled by user"), "paymentCancel");
    }

    @Operation(summary = "Handle payment error",
            description = "Handles errors during the payment process and returns an error view or JSON response.")
    @GetMapping("/payment/error")
    public Object paymentError(@RequestHeader(value = "Accept", required = false) String accept) {
        ErrorResponse response = new ErrorResponse("error", "An error occurred during the payment process", null);
        return createResponse(accept, response, "paymentError");
    }

    // Utility Methods

    private void validatePaymentParams(String paymentId, String payerId) {
        if (paymentId.isBlank() || payerId.isBlank()) {
            throw new IllegalArgumentException("Invalid paymentId or payerId");
        }
    }

    private Object createResponse(String accept, Object message, String view) {
        return isJsonAccepted(accept) ? ResponseEntity.ok(message) : view;
    }

    private Object respondBasedOnAcceptHeader(String accept, String approvalUrl) {
        return isJsonAccepted(accept)
                ? ResponseEntity.ok(new SuccessResponse("success", approvalUrl))
                : new RedirectView(approvalUrl);
    }

    private boolean isJsonAccepted(String accept) {
        return accept != null && accept.contains("application/json");
    }

    private Object handleError(String accept, String errorMessage, String errorPage, Exception e) {
        log.error("Redirecting to error page: {}. Error message: {}", errorPage, e != null ? e.getMessage() : "Unknown error");
        String errorDetails = (e != null) ? e.getMessage() : "Unknown error";
        ErrorResponse response = new ErrorResponse("error", errorMessage, errorDetails);

        if ("/payment/error".equals(errorPage)) {
            return isJsonAccepted(accept)
                    ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }

        return isJsonAccepted(accept)
                ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
                : new RedirectView(errorPage);
    }
}
