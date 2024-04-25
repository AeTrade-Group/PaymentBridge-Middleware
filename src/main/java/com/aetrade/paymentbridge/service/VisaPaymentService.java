package com.aetrade.paymentbridge.service;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class VisaPaymentService implements GatewayService {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Implementation specific to Visa
        PaymentResponse response = new PaymentResponse();
        response.setStatus("APPROVED");
        response.setResponseCode("200");
        response.setResponseMessage("Visa transaction successful");
        return response;
    }

    @Override
    public void processCallback(PaymentResponse response) {
        // Logic to handle the callback from Visa
        // For example, verify the response data and update the transaction status accordingly
    }
}