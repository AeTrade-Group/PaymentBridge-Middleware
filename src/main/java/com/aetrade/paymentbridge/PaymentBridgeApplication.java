package com.aetrade.paymentbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aetrade.paymentbridge")
public class PaymentBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentBridgeApplication.class, args);
	}

}
