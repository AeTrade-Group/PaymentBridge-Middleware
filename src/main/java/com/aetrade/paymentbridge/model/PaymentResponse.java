package com.aetrade.paymentbridge.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * This class represents a payment response that will be sent back to the marketplace.
 * It encapsulates the results and status of a payment transaction processed by REN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String transactionReference;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String customerId;
    private String status; // Status of the transaction (e.g., APPROVED, DECLINED, PENDING)
    private String responseCode; // Specific response code from the payment gateway
    private String responseMessage; // Descriptive message corresponding to the response code
    private LocalDateTime transactionDate; // The date and time when the transaction was processed
    private BigDecimal balanceAmount; // The balance amount after the transaction
    private String balanceCurrency; // The currency of the balance amount

}
