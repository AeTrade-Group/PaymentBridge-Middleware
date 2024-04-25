package com.aetrade.paymentbridge.service;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;

public interface GatewayService {
    PaymentResponse processPayment(PaymentRequest request);
    void processCallback(PaymentResponse response);
}