package com.aetrade.paymentbridge.controller;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;
import com.aetrade.paymentbridge.service.PaymentGatewayFactory;
import com.aetrade.paymentbridge.service.GatewayService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;

    private Map<String, Object> transformToRENFormat(PaymentRequest paymentRequest) {
        Map<String, Object> renRequest = new HashMap<>();
        Map<String, Object> document = new HashMap<>();
        Map<String, Object> accptrAuthstnReq = new HashMap<>();
        Map<String, Object> hdr = new HashMap<>();
        Map<String, Object> authstnReq = new HashMap<>();
        Map<String, Object> envt = new HashMap<>();
        Map<String, Object> acqrr = new HashMap<>();
        Map<String, Object> mrchnt = new HashMap<>();
        Map<String, Object> lctnAndCtct = new HashMap<>();
        Map<String, Object> pstlAdr = new HashMap<>();
        Map<String, Object> tx = new HashMap<>();
        Map<String, Object> txDtls = new HashMap<>();
        Map<String, Object> txId = new HashMap<>();

        // Header Information
        hdr.put("MsgFctn", "AUTQ");
        hdr.put("PrtcolVrsn", "2.0");
        hdr.put("CreDtTm", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        // Environment
        acqrr.put("Id", "configured_acquirer_id");
        pstlAdr.put("TwnNm", paymentRequest.getBillingAddress().get("city"));
        pstlAdr.put("CtryCd", paymentRequest.getBillingAddress().get("country"));
        pstlAdr.put("PstCd", paymentRequest.getBillingAddress().get("zip"));
        lctnAndCtct.put("PstlAdr", pstlAdr);
        mrchnt.put("Id", "configured_merchant_id");
        mrchnt.put("LctnAndCtct", lctnAndCtct);
        envt.put("Acqrr", acqrr);
        envt.put("Mrchnt", mrchnt);

        // Transaction Details
        txDtls.put("TtlAmt", paymentRequest.getAmount());
        txDtls.put("Ccy", paymentRequest.getCurrency());
        tx.put("TxDtls", txDtls);
        tx.put("TxTp", "CRDP");
        txId.put("TxDtTm", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        txId.put("TxRef", paymentRequest.getOrderId());
        tx.put("TxId", txId);

        // Assemble the request
        authstnReq.put("Envt", envt);
        authstnReq.put("Tx", tx);
        accptrAuthstnReq.put("Hdr", hdr);
        accptrAuthstnReq.put("AuthstnReq", authstnReq);
        document.put("AccptrAuthstnReq", accptrAuthstnReq);
        renRequest.put("Document", document);

        return renRequest;
    }

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

        Map<String, Object> renRequest = transformToRENFormat(paymentRequest);
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
