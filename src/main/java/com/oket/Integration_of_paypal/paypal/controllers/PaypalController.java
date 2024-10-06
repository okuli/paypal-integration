package com.oket.Integration_of_paypal.paypal.controllers;
import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

@Tag(name = "Paypal", description = "The Paypal API")
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") //In real production case, this will be restricted. It is only for
public class PaypalController {

    private final PaypalService paypalService;

    @Value("${paypal.success-url}")
    private String successUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        // Validate that configuration URLs are set during startup
        if (successUrl == null || cancelUrl == null) {
            throw new IllegalStateException("Configuration URLs for PayPal are missing");
        }
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Define Records to encapsulate responses
    public record PaymentResponse(String status, String paymentId, String payerId, String timestamp) {}
    public record ErrorResponse(String status, String message, String error, String timestamp) {}
    public record CancelResponse(String status, String message, String timestamp) {}
    public record SuccessResponse(String status, String approvalUrl, String paymentId, String timestamp) {}

    @Operation(summary = "Create a new PayPal payment", description = "Creates a new PayPal payment and returns either a JSON response or redirects to a payment approval URL.")
    @PostMapping(value = "/payment/create", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE})
    public ResponseEntity<?> createPayment(@RequestHeader(value = "Accept", required = false) Optional<String> accept) {
        try {
            String approvalUrl = paypalService.createPaymentWithApprovalUrl(
                    10.0, "USD", "paypal", "sale", "Payment description", cancelUrl, successUrl);
            return respondBasedOnAcceptHeader(accept.orElse("application/json"), approvalUrl);
        } catch (PayPalRESTException e) {
            return handleError(accept, "Payment creation failed", "/payment/error", e);
        }
    }

    @Operation(summary = "Handle successful payment", description = "Handles a successful payment and returns a success view or JSON response.")
    @GetMapping("/payment/success")
    public ResponseEntity<?> paymentSuccess(@RequestHeader(value = "Accept", required = false) Optional<String> accept,
                                            @RequestParam("paymentId") String paymentId,
                                            @RequestParam("PayerID") String payerId) {

        if (isInvalid(paymentId) || isInvalid(payerId)) {
            return handleErrorResponse(accept, "Invalid paymentId or payerId", "paymentSuccess", HttpStatus.BAD_REQUEST);
        }

        try {
            boolean isPaymentApproved = paypalService.executePaymentAndCheckState(paymentId, payerId);
            if (isPaymentApproved) {
                PaymentResponse paymentInfo = new PaymentResponse("success", paymentId, payerId, getCurrentTimestamp());
                return createResponse(accept, paymentInfo, "paymentSuccess");
            }
        } catch (PayPalRESTException e) {
            log.error("Error during payment execution: {}", e.getMessage(), e);
        }
        return handleErrorResponse(accept, "Payment execution failed", "paymentSuccess", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(summary = "Handle payment cancellation", description = "Handles a cancelled payment and returns either a cancellation view or a JSON response.")
    @GetMapping("/payment/cancel")
    public ResponseEntity<?> paymentCancel(@RequestHeader(value = "Accept", required = false) Optional<String> accept) {
        return createResponse(accept, new CancelResponse("cancelled", "Payment cancelled by user", getCurrentTimestamp()), "paymentCancel");
    }

    @Operation(summary = "Handle payment error", description = "Handles errors during the payment process and returns an error view or JSON response.")
    @GetMapping("/payment/error")
    public ResponseEntity<?> paymentError(@RequestHeader(value = "Accept", required = false) Optional<String> accept) {
        return createResponse(accept, new ErrorResponse("error", "An error occurred during the payment process", null, getCurrentTimestamp()), "paymentError");
    }

    // Utility Methods

    private boolean isInvalid(String value) {
        return value == null || value.isEmpty();
    }

    private ResponseEntity<?> createResponse(Optional<String> accept, Object message, Object view) {
        return (accept.isPresent() && accept.get().contains("application/json"))
                ? ResponseEntity.ok(message)
                : ResponseEntity.status(HttpStatus.OK).body(view); // Assuming view returns an appropriate response
    }

    private ResponseEntity<?> respondBasedOnAcceptHeader(String accept, String approvalUrl) {
        if (accept.contains("application/json")) {
            return ResponseEntity.ok(new SuccessResponse("success", approvalUrl, null, getCurrentTimestamp()));
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(approvalUrl)).build(); // Use FOUND for redirects
    }

    private ResponseEntity<?> handleError(Optional<String> accept, String errorMessage, String errorPage, Exception e) {
        log.error("Redirecting to error page: {} due to error: {}", errorPage, e != null ? e.getMessage() : "Unknown error");
        String errorDetails = (e != null) ? e.getMessage() : "Unknown error";
        ErrorResponse response = new ErrorResponse("error", errorMessage, errorDetails, getCurrentTimestamp());
        return (accept.isPresent() && accept.get().contains("application/json"))
                ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
                : ResponseEntity.status(HttpStatus.FOUND).location(URI.create(errorPage)).build();
    }

    private ResponseEntity<?> handleErrorResponse(Optional<String> accept, String errorMessage, String errorPage, HttpStatus status) {
        return handleError(accept, errorMessage, errorPage, null);
    }

    private String getCurrentTimestamp() {
        return Instant.now().toString(); // Adjust format as necessary
    }
}