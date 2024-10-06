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

@Tag(name = "Paypal", description = "The Paypal API") // OpenAPI documentation tag for grouping and describing the API.
@Controller // Marks this class as a Spring MVC Controller that handles HTTP requests.
@RequestMapping("/") // Base URL for the controller's endpoints.
@RequiredArgsConstructor // Generates a constructor with required fields (i.e., final fields).
@Slf4j // Lombok annotation to automatically generate a logger for logging messages.
@CrossOrigin(origins = "*") // Allows cross-origin requests from all sources, which should be restricted for security.
public class PaypalController {

    private final PaypalService paypalService; // Dependency injection for PayPal service, which contains business logic.

    @Value("${paypal.success-url}") // Injects success URL value from configuration.
    private String successUrl;

    @Value("${paypal.cancel-url}") // Injects cancel URL value from configuration.
    private String cancelUrl;

    @GetMapping("/") // Handles GET requests to the root URL ("/").
    public String home() {
        return "index"; // Returns the "index" view (usually a static webpage).
    }

    // Define response record types to encapsulate different response formats (JSON, view names, etc.)
    public record PaymentResponse(String status, String paymentId, String payerId) {}
    public record ErrorResponse(String status, String message, String error) {}
    public record CancelResponse(String status, String message) {}
    public record SuccessResponse(String status, String approvalUrl) {}

    @Operation(summary = "Create a new PayPal payment", // OpenAPI annotation to describe the API endpoint.
            description = "Creates a new PayPal payment and returns either a JSON response or redirects to a payment approval URL.")
    @PostMapping(value = "/payment/create", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE})
    // Handles POST requests to "/payment/create", supporting JSON and HTML response formats.
    public Object createPayment(@RequestHeader(value = "Accept", required = false) String accept) {
        validateConfigUrls(); // Validates the success and cancel URLs.

        try {
            // Calls the service to create a PayPal payment and get the approval URL.
            String approvalUrl = paypalService.createPaymentWithApprovalUrl(
                    10.0, "USD", "paypal", "sale", "Payment description", cancelUrl, successUrl);
            // Returns the response based on the "Accept" header (either JSON or redirects to the approval URL).
            return respondBasedOnAcceptHeader(accept, approvalUrl);
        } catch (PayPalRESTException e) {
            // Logs the error and handles the failure case.
            log.error("Error occurred while creating payment. Accept: {}, Error: {}", accept, e.getMessage(), e);
            return handleError(accept, "Payment creation failed", "/payment/error", e);
        }
    }

    @Operation(summary = "Handle successful payment", // OpenAPI documentation for handling successful payments.
            description = "Handles a successful payment and returns a success view or JSON response.")
    @GetMapping("/payment/success") // Handles GET requests to "/payment/success".
    public Object paymentSuccess(@RequestHeader(value = "Accept", required = false) String accept,
                                 @RequestParam("paymentId") String paymentId, // Payment ID query parameter.
                                 @RequestParam("PayerID") String payerId) { // Payer ID query parameter.
        validatePaymentParams(paymentId, payerId); // Validates the paymentId and payerId.

        try {
            // Executes the payment through the service and checks if it's approved.
            boolean isPaymentApproved = paypalService.executePaymentAndCheckState(paymentId, payerId);
            if (isPaymentApproved) {
                // Creates a response object with the payment details.
                PaymentResponse paymentInfo = new PaymentResponse("success", paymentId, payerId);
                // Returns a response based on the "Accept" header.
                return createResponse(accept, paymentInfo, "paymentSuccess");
            }
        } catch (PayPalRESTException e) {
            // Logs any error that occurred during payment execution.
            log.error("Error during payment execution. PaymentId: {}, PayerId: {}, Error: {}", paymentId, payerId, e.getMessage(), e);
        }
        // If an error occurs or payment is not approved, it handles the error.
        return handleError(accept, "Payment execution failed", "paymentSuccess", null);
    }

    @Operation(summary = "Handle payment cancellation", // OpenAPI documentation for handling payment cancellations.
            description = "Handles a cancelled payment and returns either a cancellation view or a JSON response.")
    @GetMapping("/payment/cancel") // Handles GET requests to "/payment/cancel".
    public Object paymentCancel(@RequestHeader(value = "Accept", required = false) String accept) {
        // Creates a cancellation response object.
        CancelResponse response = new CancelResponse("cancelled", "Payment cancelled by user");
        // Returns a response based on the "Accept" header.
        return createResponse(accept, response, "paymentCancel");
    }

    @Operation(summary = "Handle payment error", // OpenAPI documentation for handling payment errors.
            description = "Handles errors during the payment process and returns an error view or JSON response.")
    @GetMapping("/payment/error") // Handles GET requests to "/payment/error".
    public Object paymentError(@RequestHeader(value = "Accept", required = false) String accept) {
        // Creates an error response object.
        ErrorResponse response = new ErrorResponse("error", "An error occurred during the payment process", null);
        // Returns a response based on the "Accept" header.
        return createResponse(accept, response, "paymentError");
    }

    // Utility Methods

    // Validates that the cancel and success URLs are configured.
    private void validateConfigUrls() {
        if (cancelUrl == null || successUrl == null) {
            throw new IllegalArgumentException("Missing configuration URLs");
        }
    }

    // Validates the paymentId and payerId for non-empty values.
    private void validatePaymentParams(String paymentId, String payerId) {
        if (paymentId.isBlank() || payerId.isBlank()) {
            throw new IllegalArgumentException("Invalid paymentId or payerId");
        }
    }

    // Creates a response object based on the "Accept" header (either JSON or view name).
    private Object createResponse(String accept, Object message, String view) {
        return isJsonAccepted(accept) ? ResponseEntity.ok(message) : view; // Responds with JSON or view.
    }

    // Returns either a JSON response or a redirect based on the "Accept" header.
    private Object respondBasedOnAcceptHeader(String accept, String approvalUrl) {
        return isJsonAccepted(accept)
                ? ResponseEntity.ok(new SuccessResponse("success", approvalUrl)) // JSON response.
                : new RedirectView(approvalUrl); // Redirects to PayPal approval URL.
    }

    // Checks if the "Accept" header includes JSON content type.
    private boolean isJsonAccepted(String accept) {
        return accept != null && accept.contains("application/json");
    }

    // Handles errors by logging and responding with appropriate status and messages.
    private Object handleError(String accept, String errorMessage, String errorPage, Exception e) {
        log.error("Redirecting to error page: {}. Error message: {}", errorPage, e != null ? e.getMessage() : "Unknown error");
        String errorDetails = (e != null) ? e.getMessage() : "Unknown error"; // Logs error details.
        ErrorResponse response = new ErrorResponse("error", errorMessage, errorDetails); // Creates an error response.

        // Avoids redirection loops by sending a JSON response if the user is already on the error page.
        if ("/payment/error".equals(errorPage)) {
            return isJsonAccepted(accept)
                    ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response) // JSON response for error.
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred."); // Text response.
        }

        return isJsonAccepted(accept)
                ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response) // JSON error response.
                : new RedirectView(errorPage); // Redirects to the error page.
    }
}
