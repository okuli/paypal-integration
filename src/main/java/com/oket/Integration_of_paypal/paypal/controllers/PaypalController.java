package com.oket.Integration_of_paypal.paypal.controllers;

import com.oket.Integration_of_paypal.paypal.services.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
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
import java.util.*;

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

    @Operation(summary = "Create a new PayPal payment",
            description = "Creates a new PayPal payment and returns either a JSON response or redirects to a payment approval URL.")
    @PostMapping(value = "/payment/create", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE})
    public Object createPayment(@RequestHeader(value = "Accept", required = false) String accept) {
        try {
            Payment payment = paypalService.createPayment(
                    10.0, "USD", "paypal", "sale",
                    "Payment description", cancelUrl, successUrl
            );

            String approvalUrl = getApprovalUrl(payment);
            if (approvalUrl == null) {
                throw new PayPalRESTException("Approval URL not found");
            }

            // Return JSON response or Redirect to approval URL
            return respondBasedOnAcceptHeader(accept, approvalUrl, payment.getId());
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
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if ("approved".equals(payment.getState())) {
                Map<String, Object> paymentInfo = Map.of("paymentId", paymentId, "payerId", payerId);
                return createResponse(accept, "success", paymentInfo, "paymentSuccess");
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
        Map<String, Object> response = Map.of("status", "cancelled", "message", "Payment cancelled by user");
        return createResponse(accept, response, "paymentCancel");
    }

    @Operation(summary = "Handle payment error",
            description = "Handles errors during the payment process and returns an error view or JSON response.")
    @GetMapping("/payment/error")
    public Object paymentError(@RequestHeader(value = "Accept", required = false) String accept) {
        Map<String, Object> response = Map.of("status", "error", "message", "An error occurred during the payment process");
        return createResponse(accept, response, "paymentError");
    }

    // Utility Methods

    private String getApprovalUrl(Payment payment) {
        return payment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .map(Links::getHref)
                .findFirst()
                .orElse(null);
    }

    private Object createResponse(String accept, Map<String, Object> message, Object view) {
        return accept != null && accept.contains("application/json")
                ? ResponseEntity.ok(message)
                : view;
    }

    private Object createResponse(String accept, String status, Map<String, Object> message, Object view) {
        Map<String, Object> response = new HashMap<>(message);
        response.put("status", status);
        return createResponse(accept, response, view);
    }

    private Object respondBasedOnAcceptHeader(String accept, String approvalUrl, String paymentId) {
        if (accept != null && accept.contains("application/json")) {
            Map<String, Object> response = Map.of("status", "success", "approval_url", approvalUrl, "paymentId", paymentId);
            return ResponseEntity.ok(response);
        }
        return new RedirectView(approvalUrl);
    }

    private Object handleError(String accept, String errorMessage, String errorPage, Exception e) {
        log.error("Redirecting to error page: {}", errorPage);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", errorMessage);
        if (e instanceof PayPalRESTException pex) {
            response.put("error", pex.getDetails() != null ? pex.getDetails().getMessage() : e.getMessage());
        }

        return accept != null && accept.contains("application/json")
                ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
                : new RedirectView(errorPage);
    }

    private Object handleErrorResponse(String accept, String errorMessage, String errorPage) {
        return handleError(accept, errorMessage, errorPage, null);
    }
}