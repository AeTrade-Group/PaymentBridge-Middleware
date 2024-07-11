package com.aetrade.paymentbridge.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;
import com.aetrade.paymentbridge.service.GatewayService;
import com.aetrade.paymentbridge.service.PaymentGatewayFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;


    /**
     * Endpoint to initiate a payment. This method receives a payment request,
     * determines the appropriate gateway service based on the payment method,
     * and processes the payment.
     *
     * @param paymentRequest The payment request details from CS-Cart.
     * @return ResponseEntity containing the PaymentResponse, including status and message.
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest paymentRequest) {
        GatewayService gatewayService = paymentGatewayFactory.getPaymentService(paymentRequest.getPaymentMethod());
        
        if (gatewayService == null) {
            PaymentResponse response = new PaymentResponse();
            response.setStatus("DECLINED");
            response.setResponseMessage("Unsupported payment method");
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Object> renRequest = gatewayService.transformToRENFormat(paymentRequest);
        PaymentResponse response = gatewayService.processPayment(renRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to handle asynchronous callbacks from the payment gateway.
     * This method processes updates from the gateway about the payment status.
     *
     * @param paymentResponse The payment response from the gateway.
     * @return ResponseEntity indicating the result of processing the callback.
     */
    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(@RequestBody PaymentResponse paymentResponse) {
        // Assuming the paymentResponse includes enough information to determine the gateway
        GatewayService gatewayService = paymentGatewayFactory.getPaymentService(paymentResponse.getPaymentMethod());
        
        if (gatewayService == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported payment method in callback");
        }

        // A method to process callbacks should be implemented in the GatewayService or handled here
        try {
            gatewayService.processCallback(paymentResponse);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing callback: " + e.getMessage());
        }
    }
}
