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
@Override
public Map<String, Object> transformToRENFormat(PaymentRequest paymentRequest) {
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
