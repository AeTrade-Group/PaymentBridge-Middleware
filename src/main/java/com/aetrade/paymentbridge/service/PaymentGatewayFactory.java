package com.aetrade.paymentbridge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentGatewayFactory {

    @Autowired
    private MTNMobileMoneyService mtnMobileMoneyService;

    @Autowired
    private VisaPaymentService visaPaymentService;

    private Map<String, GatewayService> gatewayServices;

    @PostConstruct
    public void init() {
        gatewayServices = new HashMap<>();
        gatewayServices.put("MTN Mobile Money", mtnMobileMoneyService);
        gatewayServices.put("Visa", visaPaymentService);
        // Add more services as needed
    }

    public GatewayService getPaymentService(String paymentMethod) {
        return gatewayServices.getOrDefault(paymentMethod, null);
    }
}
