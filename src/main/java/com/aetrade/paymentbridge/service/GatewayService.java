package com.aetrade.paymentbridge.service;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;

import java.util.Map;

public interface GatewayService {
    PaymentResponse processPayment(Map<String, Object> renRequest);
    void processCallback(PaymentResponse response);
    Map<String, Object> transformToRENFormat(PaymentRequest paymentRequest);
}
