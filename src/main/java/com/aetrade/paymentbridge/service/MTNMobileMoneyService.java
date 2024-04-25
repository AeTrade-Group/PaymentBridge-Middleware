package com.aetrade.paymentbridge.service;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class MTNMobileMoneyService implements GatewayService {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Implementation specific to MTN Mobile Money
        PaymentResponse response = new PaymentResponse();
        response.setStatus("APPROVED");
        response.setResponseCode("200");
        response.setResponseMessage("MTN Mobile Money transaction successful");
        return response;
    }

    @Override
    public void processCallback(PaymentResponse response) {
        // Logic to handle the callback from MTN Mobile Money
        // For example, update the transaction status in the database based on the response
    }
}
