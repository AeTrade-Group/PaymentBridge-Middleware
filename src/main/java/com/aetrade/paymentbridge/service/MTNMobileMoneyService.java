package com.aetrade.paymentbridge.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aetrade.paymentbridge.model.PaymentRequest;
import com.aetrade.paymentbridge.model.PaymentResponse;
import com.aetrade.paymentbridge.model.Transaction;
import com.aetrade.paymentbridge.repository.TransactionRepository;

@Service
public class MTNMobileMoneyService implements GatewayService {

	@Autowired
	private TransactionRepository transactionRepository;

	@Override
	public PaymentResponse processPayment(Map<String, Object> renRequest) {
		// Implementation specific to MTN Mobile Money
		PaymentResponse response = new PaymentResponse();
		// Extract necessary fields from the REN response
		Map<String, Object> document = (Map<String, Object>) renRequest.get("Document");
		Map<String, Object> accptrAuthstnRspn = (Map<String, Object>) document.get("AccptrAuthstnRspn");
		if (accptrAuthstnRspn == null) {
			throw new IllegalArgumentException("Invalid REN response: AccptrAuthstnRspn is missing");
		}
		Map<String, Object> hdr = (Map<String, Object>) accptrAuthstnRspn.get("Hdr");
		Map<String, Object> authstnRspn = (Map<String, Object>) accptrAuthstnRspn.get("AuthstnRspn");
		if (authstnRspn == null) {
			throw new IllegalArgumentException("Invalid REN response: AuthstnRspn is missing");
		}
		Map<String, Object> txRspn = (Map<String, Object>) authstnRspn.get("TxRspn");
		if (txRspn == null) {
			throw new IllegalArgumentException("Invalid REN response: TxRspn is missing");
		}
		Map<String, Object> authstnRslt = (Map<String, Object>) txRspn.get("AuthstnRslt");
		if (authstnRslt == null) {
			throw new IllegalArgumentException("Invalid REN response: AuthstnRslt is missing");
		}
		Map<String, Object> rspnToAuthstn = (Map<String, Object>) authstnRslt.get("RspnToAuthstn");
		if (rspnToAuthstn == null) {
			throw new IllegalArgumentException("Invalid REN response: RspnToAuthstn is missing");
		}

		String responseCode = (String) rspnToAuthstn.get("Rspn");
		String responseMessage = (String) rspnToAuthstn.get("RspnRsn");

		if ("APPR".equals(responseCode)) {
			response.setStatus("APPROVED");
			response.setResponseCode("200");
			response.setResponseMessage("MTN Mobile Money transaction successful");
		} else {
			response.setStatus("DECLINED");
			response.setResponseCode("400");
			response.setResponseMessage(
					responseMessage != null ? responseMessage : "MTN Mobile Money transaction failed");
		}

		// Additional fields for wallet transactions
		if (txRspn.containsKey("Bal")) {
			Map<String, Object> balance = (Map<String, Object>) txRspn.get("Bal");
			response.setBalanceAmount(new BigDecimal((String) balance.get("Amt")));
			response.setBalanceCurrency((String) balance.get("Ccy"));
		}

		return response;
	}

	@Override
	public void processCallback(PaymentResponse response) {
		// Logic to handle the callback from MTN Mobile Money
		// For example, update the transaction status in the database based on the
		// response
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
		hdr.put("Chnl", "WEB");

		// Environment
		acqrr.put("Id", Map.of("Id", "500008"));
		pstlAdr.put("TwnNm", paymentRequest.getBillingAddress().get("city"));
		pstlAdr.put("CtrySubDvsn", "MZ");
		pstlAdr.put("CtryCd", paymentRequest.getBillingAddress().get("country"));
		pstlAdr.put("PstCd", paymentRequest.getBillingAddress().get("zip"));
		lctnAndCtct.put("PstlAdr", pstlAdr);
		mrchnt.put("Id", "000000081111111");
		mrchnt.put("LctnAndCtct", lctnAndCtct);
		mrchnt.put("CmonNm", "SIM Av. 24 de Julho 155");
		envt.put("Acqrr", acqrr);
		envt.put("Mrchnt", mrchnt);
		envt.put("POI", Map.of("Id", "SbmtTrn"));
		envt.put("Wllt", Map.of("Id", "081234", "Prvdr", "OPR1  "));

		// Context
		Map<String, Object> cntxt = new HashMap<>();
		cntxt.put("PmtCntxt", Map.of("AttndncCntxt", "UATT"));

		// Transaction Details
		txDtls.put("TtlAmt", paymentRequest.getAmount());
		txDtls.put("Ccy", paymentRequest.getCurrency());
		tx.put("TxDtls", txDtls);
		tx.put("TxTp", "CRDP");
		tx.put("MrchntCtgyCd", "6012");
		txId.put("TxDtTm", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		txId.put("TxRef", paymentRequest.getOrderId());
		tx.put("TxId", txId);
		tx.put("AcctFr", Map.of("SelctdAcctTp", "WLLT"));

		// Assemble the request
		authstnReq.put("Envt", envt);
		authstnReq.put("Cntxt", cntxt);
		authstnReq.put("Tx", tx);
		accptrAuthstnReq.put("Hdr", hdr);
		accptrAuthstnReq.put("AuthstnReq", authstnReq);
		document.put("AccptrAuthstnReq", accptrAuthstnReq);
		renRequest.put("Document", document);

		return renRequest;
	}

	@Override
	public void saveTransaction(PaymentRequest paymentRequest, PaymentResponse paymentResponse) {
		Transaction transaction = new Transaction();
		transaction.setTransactionReference(paymentRequest.getTransactionReference());
		transaction.setAmount(paymentRequest.getAmount());
		transaction.setCurrency(paymentRequest.getCurrency());
		transaction.setPaymentMethod(paymentRequest.getPaymentMethod());
		transaction.setCustomerId(paymentRequest.getCustomerId());
		transaction.setStatus(paymentResponse.getStatus());
		transaction.setResponseCode(paymentResponse.getResponseCode());
		transaction.setResponseMessage(paymentResponse.getResponseMessage());
		transactionRepository.save(transaction);
	}
}
