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
        Map<String, Object> hdr = new HashMap<>();
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
        hdr.put("MsgFctn", "AUTQ");
        accptrAuthstnRspn.put("Hdr", hdr);
        hdr.put("MsgFctn", "AUTQ");
        accptrAuthstnRspn.put("Hdr", hdr);
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
import java.time.LocalDateTime;
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
        // Mock REN request for a successful transaction
        Map<String, Object> renRequest = new HashMap<>();
        Map<String, Object> document = new HashMap<>();
        Map<String, Object> accptrAuthstnRspn = new HashMap<>();
        Map<String, Object> hdr = new HashMap<>();
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
        hdr.put("MsgFctn", "AUTQ");
        accptrAuthstnRspn.put("Hdr", hdr);
        authstnRspn.put("TxRspn", txRspn);
        accptrAuthstnRspn.put("AuthstnRspn", authstnRspn);
        document.put("AccptrAuthstnRspn", accptrAuthstnRspn);
        renRequest.put("Document", document);

        // Process payment
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
        // Mock REN request for a failed transaction
        Map<String, Object> renRequest = new HashMap<>();
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
        renRequest.put("Document", document);

        // Process payment
        PaymentResponse response = mtnMobileMoneyService.processPayment(renRequest);

        // Verify the response
        assertEquals("DECLINED", response.getStatus());
        assertEquals("400", response.getResponseCode());
        assertEquals("Insufficient Fund", response.getResponseMessage());
    }

    @Test
    public void testProcessCallback() {
        // Mock PaymentResponse
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setTransactionReference("TXN123456789");
        paymentResponse.setStatus("APPROVED");
        paymentResponse.setResponseCode("200");
        paymentResponse.setResponseMessage("Transaction successful");

        // Mock Transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionReference("TXN123456789");
        when(transactionRepository.findByTransactionReference("TXN123456789")).thenReturn(transaction);

        // Process callback
        mtnMobileMoneyService.processCallback(paymentResponse);

        // Verify the transaction update
        verify(transactionRepository, times(1)).save(transaction);
        assertEquals("APPROVED", transaction.getStatus());
        assertEquals("200", transaction.getResponseCode());
        assertEquals("Transaction successful", transaction.getResponseMessage());
    }

    @Test
    public void testTransformToRENFormat() {
        // Create a valid PaymentRequest
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

        // Transform to REN format
        Map<String, Object> renRequest = mtnMobileMoneyService.transformToRENFormat(paymentRequest);

        // Verify the REN request structure
        assertEquals("AUTQ", ((Map<String, Object>) ((Map<String, Object>) renRequest.get("Document")).get("AccptrAuthstnReq")).get("Hdr").get("MsgFctn"));
        assertEquals("943", ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) renRequest.get("Document")).get("AccptrAuthstnReq")).get("AuthstnReq")).get("Tx")).get("TxDtls")).get("Ccy"));
    }

    @Test
    public void testSaveTransaction() {
        // Create a valid PaymentRequest
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("500"));
        paymentRequest.setCurrency("943");
        paymentRequest.setPaymentMethod("MTN Mobile Money");
        paymentRequest.setTransactionReference("0123456312");
        paymentRequest.setCallbackUrl("http://callback.url");
        paymentRequest.setCustomerId("customer123");
        paymentRequest.setOrderId("order123");
        paymentRequest.setUserId("user123");

        // Create a valid PaymentResponse
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("APPROVED");
        paymentResponse.setResponseCode("200");
        paymentResponse.setResponseMessage("MTN Mobile Money transaction successful");

        // Save transaction
        mtnMobileMoneyService.saveTransaction(paymentRequest, paymentResponse);

        // Verify the transaction save
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
