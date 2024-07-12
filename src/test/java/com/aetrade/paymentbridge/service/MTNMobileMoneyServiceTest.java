package com.aetrade.paymentbridge.service;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;
import com.aetrade.paymentbridge.model.Transaction;
import com.aetrade.paymentbridge.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MTNMobileMoneyServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private MTNMobileMoneyService mtnMobileMoneyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessPayment_Success() {
        // Mock REN response for a successful transaction
        Map<String, Object> renResponse = new HashMap<>();
        Map<String, Object> document = new HashMap<>();
        Map<String, Object> accptrAuthstnRspn = new HashMap<>();
        Map<String, Object> authstnRspn = new HashMap<>();
        Map<String, Object> txRspn = new HashMap<>();
        Map<String, Object> authstnRslt = new HashMap<>();
        Map<String, Object> rspnToAuthstn = new HashMap<>();
        Map<String, Object> balance = new HashMap<>();

        rspnToAuthstn.put("Rspn", "APPR");
        authstnRslt.put("RspnToAuthstn", rspnToAuthstn);
        balance.put("Amt", "3577");
        balance.put("Ccy", "943");
        txRspn.put("AuthstnRslt", authstnRslt);
        txRspn.put("Bal", balance);
        authstnRspn.put("TxRspn", txRspn);
        accptrAuthstnRspn.put("AuthstnRspn", authstnRspn);
        document.put("AccptrAuthstnRspn", accptrAuthstnRspn);
        renResponse.put("Document", document);

        // Create a valid CS-Cart request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("500"));
        paymentRequest.setCurrency("943");
        paymentRequest.setPaymentMethod("MTN Mobile Money");
        paymentRequest.setTransactionReference("0123456312");
        paymentRequest.setCallbackUrl("http://callback.url");
        paymentRequest.setCustomerId("customer123");
        paymentRequest.setOrderId("order123");
        paymentRequest.setUserId("user123");
        Map<String, String> billingAddress = new HashMap<>();
        billingAddress.put("city", "Maputo");
        billingAddress.put("country", "MZ");
        billingAddress.put("zip", "1100");
        paymentRequest.setBillingAddress(billingAddress);

        // Transform to REN format and process payment
        Map<String, Object> renRequest = mtnMobileMoneyService.transformToRENFormat(paymentRequest);
        PaymentResponse response = mtnMobileMoneyService.processPayment(renRequest);

        // Verify the response
        assertEquals("APPROVED", response.getStatus());
        assertEquals("200", response.getResponseCode());
        assertEquals("MTN Mobile Money transaction successful", response.getResponseMessage());
        assertEquals(new BigDecimal("3577"), response.getBalanceAmount());
        assertEquals("943", response.getBalanceCurrency());
    }

    @Test
    public void testProcessPayment_Failure() {
        // Mock REN response for a failed transaction
        Map<String, Object> renResponse = new HashMap<>();
        Map<String, Object> document = new HashMap<>();
        Map<String, Object> accptrAuthstnRspn = new HashMap<>();
        Map<String, Object> authstnRspn = new HashMap<>();
        Map<String, Object> txRspn = new HashMap<>();
        Map<String, Object> authstnRslt = new HashMap<>();
        Map<String, Object> rspnToAuthstn = new HashMap<>();

        rspnToAuthstn.put("Rspn", "DECL");
        rspnToAuthstn.put("RspnRsn", "Insufficient Fund");
        authstnRslt.put("RspnToAuthstn", rspnToAuthstn);
        txRspn.put("AuthstnRslt", authstnRslt);
        authstnRspn.put("TxRspn", txRspn);
        accptrAuthstnRspn.put("AuthstnRspn", authstnRspn);
        document.put("AccptrAuthstnRspn", accptrAuthstnRspn);
        renResponse.put("Document", document);

        // Create a valid CS-Cart request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("8701"));
        paymentRequest.setCurrency("943");
        paymentRequest.setPaymentMethod("MTN Mobile Money");
        paymentRequest.setTransactionReference("0123456312");
        paymentRequest.setCallbackUrl("http://callback.url");
        paymentRequest.setCustomerId("customer123");
        paymentRequest.setOrderId("order123");
        paymentRequest.setUserId("user123");
        Map<String, String> billingAddress = new HashMap<>();
        billingAddress.put("city", "Maputo");
        billingAddress.put("country", "MZ");
        billingAddress.put("zip", "1100");
        paymentRequest.setBillingAddress(billingAddress);

        // Transform to REN format and process payment
        Map<String, Object> renRequest = mtnMobileMoneyService.transformToRENFormat(paymentRequest);
        PaymentResponse response = mtnMobileMoneyService.processPayment(renRequest);

        // Verify the response
        assertEquals("DECLINED", response.getStatus());
        assertEquals("400", response.getResponseCode());
        assertEquals("Insufficient Fund", response.getResponseMessage());
    }

    @Test
    public void testSaveTransaction() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("500"));
        paymentRequest.setCurrency("943");
        paymentRequest.setPaymentMethod("MTN Mobile Money");
        paymentRequest.setTransactionReference("0123456312");
        paymentRequest.setCallbackUrl("http://callback.url");
        paymentRequest.setCustomerId("customer123");
        paymentRequest.setOrderId("order123");
        paymentRequest.setUserId("user123");

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("APPROVED");
        paymentResponse.setResponseCode("200");
        paymentResponse.setResponseMessage("MTN Mobile Money transaction successful");

        mtnMobileMoneyService.saveTransaction(paymentRequest, paymentResponse);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
