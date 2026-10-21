package com.payguard.payment.service;

import com.payguard.payment.client.FraudClient;
import com.payguard.payment.dto.ChargeRequest;
import com.payguard.payment.dto.TransactionResponse;
import com.payguard.payment.entity.Transaction;
import com.payguard.payment.enums.TransactionStatus;
import com.payguard.payment.exception.PaymentBlockedException;
import com.payguard.payment.repository.TransactionRepository;
import com.payguard.shared.fraud.FraudScoreRequest;
import com.payguard.shared.fraud.FraudScoreResult;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final YooKassaService yooKassaService;
    private final TransactionRepository transactionRepository;
    private final FraudClient fraudClient;

    @Transactional
    public TransactionResponse processPayment(UUID merchantId, @Valid ChargeRequest request) {
        // 1. Save new payment transaction with status pending
        Transaction transaction = Transaction.builder()
                .merchantId(merchantId)
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .customerEmail(request.customerEmail().toLowerCase().trim())
                .description(request.description())
                .metadata(request.metadata())
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 1.1 Create Kafka event payment transaction created
        // TODO:
        log.info("Transaction created. Amount {}, merchantId {}, customerEmail {}", request.amount(), merchantId, request.customerEmail());

        // 2. Fraud-check REST request
        FraudScoreResult fraudScoreResult = fraudClient.score(FraudScoreRequest.builder()
                .transactionId(savedTransaction.getId())
                .merchantId(merchantId)
                .currency(savedTransaction.getCurrency())
                .customerEmail(savedTransaction.getCustomerEmail())
                .build());

        if (fraudScoreResult == null) throw new RuntimeException(); // TODO: Think about it

        // 3. CHARGE/HUMAN CHECK/BLOCK
        String paymentUrl = null;
        switch (fraudScoreResult.decision()) {
            case BLOCK ->
                    throw new PaymentBlockedException(savedTransaction.getId().toString(), fraudScoreResult.fraudScore());
            // TODO: implement logic for this one
            case REVIEW ->
                    throw new PaymentBlockedException(savedTransaction.getId().toString(), fraudScoreResult.fraudScore());
            case APPROVE -> paymentUrl = yooKassaService.chargeViaYooKassa(request);
        }


        return TransactionResponse.builder()
                .amount(request.amount())
                .customerEmail(request.customerEmail())
                .fraudDecision(fraudScoreResult.decision())
                .paymentUrl(paymentUrl)
                .build();
    }
}
