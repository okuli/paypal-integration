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

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Define Records to encapsulate responses
    public record PaymentResponse(String status, String paymentId, String payerId) {}
    public record ErrorResponse(String status, String message, String error) {}
    public record CancelResponse(String status, String message) {}
    public record SuccessResponse(String status, String approvalUrl, String paymentId) {}

    @Operation(summary = "Create a new PayPal payment",
            description = "Creates a new PayPal payment and returns either a JSON response or redirects to a payment approval URL.")
    @PostMapping(value = "/payment/create", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE})
    public Object createPayment(@RequestHeader(value = "Accept", required = false) String accept) {
        // Basic validation example
        if (cancelUrl == null || successUrl == null) {
            return handleError(accept, "Missing configuration URLs", "/payment/error", null);
        }

        try {
            String approvalUrl = paypalService.createPaymentWithApprovalUrl(10.0, "USD", "paypal", "sale",
                    "Payment description", cancelUrl, successUrl);
            return respondBasedOnAcceptHeader(accept, approvalUrl);
        } catch (PayPalRESTException e) {
            log.error("Error occurred while creating payment", e);
            return handleError(accept, "Payment creation failed", "/payment/error", e);
        }
    }

    @Operation(summary = "Handle successful payment",
            description = "Handles a successful payment and returns a success view or JSON response.")
    @GetMapping("/payment/success")
    public Object paymentSuccess(
            @RequestHeader(value = "Accept", required = false) String accept,
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {

        // Validation of paymentId and payerId
        if (paymentId == null || paymentId.isEmpty() || payerId == null || payerId.isEmpty()) {
            return handleErrorResponse(accept, "Invalid paymentId or payerId", "paymentSuccess");
        }

        try {
            boolean isPaymentApproved = paypalService.executePaymentAndCheckState(paymentId, payerId);
            if (isPaymentApproved) {
                PaymentResponse paymentInfo = new PaymentResponse("success", paymentId, payerId);
                return createResponse(accept, paymentInfo, "paymentSuccess");
            }
        } catch (PayPalRESTException e) {
            log.error("Error during payment execution", e);
        }
        return handleErrorResponse(accept, "Payment execution failed", "paymentSuccess");
    }

    @Operation(summary = "Handle payment cancellation",
            description = "Handles a cancelled payment and returns either a cancellation view or a JSON response.")
    @GetMapping("/payment/cancel")
    public Object paymentCancel(@RequestHeader(value = "Accept", required = false) String accept) {
        CancelResponse response = new CancelResponse("cancelled", "Payment cancelled by user");
        return createResponse(accept, response, "paymentCancel");
    }

    @Operation(summary = "Handle payment error",
            description = "Handles errors during the payment process and returns an error view or JSON response.")
    @GetMapping("/payment/error")
    public Object paymentError(@RequestHeader(value = "Accept", required = false) String accept) {
        ErrorResponse response = new ErrorResponse("error", "An error occurred during the payment process", null);
        return createResponse(accept, response, "paymentError");
    }

    // Utility Methods

    private Object createResponse(String accept, Object message, Object view) {
        return accept != null && accept.contains("application/json")
                ? ResponseEntity.ok(message)
                : view;
    }

    private Object respondBasedOnAcceptHeader(String accept, String approvalUrl) {
        if (accept != null && accept.contains("application/json")) {
            SuccessResponse response = new SuccessResponse("success", approvalUrl, null);
            return ResponseEntity.ok(response);
        }
        return new RedirectView(approvalUrl);
    }

    private Object handleError(String accept, String errorMessage, String errorPage, Exception e) {
        log.error("Redirecting to error page: {}", errorPage);
        String errorDetails = e != null ? e.getMessage() : "Unknown error";
        ErrorResponse response = new ErrorResponse("error", errorMessage, errorDetails);

        return accept != null && accept.contains("application/json")
                ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
                : new RedirectView(errorPage);
    }

    private Object handleErrorResponse(String accept, String errorMessage, String errorPage) {
        return handleError(accept, errorMessage, errorPage, null);
    }
}
