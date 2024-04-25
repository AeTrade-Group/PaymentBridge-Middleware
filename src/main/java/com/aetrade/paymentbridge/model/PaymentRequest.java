package com.aetrade.paymentbridge.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents a payment initiation request from the marketplace.
 * It captures all the necessary information needed to process a payment through REN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency should be a 3-letter ISO code")
    private String currency;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotBlank(message = "Callback URL is required")
    private String callbackUrl;

    @NotBlank(message = "Customer ID is required")
    @Size(min = 1, max = 50, message = "Customer ID must be between 1 and 50 characters")
    private String customerId;

}