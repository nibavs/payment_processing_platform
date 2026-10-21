package com.payguard.payment.service;

import com.payguard.payment.client.FraudClient;
import com.payguard.payment.dto.ChargeRequest;
import com.payguard.payment.dto.TransactionResponse;
import com.payguard.payment.entity.Transaction;
import com.payguard.payment.exception.PaymentBlockedException;
import com.payguard.payment.repository.TransactionRepository;
import com.payguard.shared.fraud.FraudScoreRequest;
import com.payguard.shared.fraud.FraudScoreResponse;
import com.payguard.shared.payment.TransactionStatus;
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
    private final PaymentEventPublisher paymentEventPublisher;

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

        // 2. Fraud-check REST request
        FraudScoreResponse fraudScoreResponse = fraudClient.score(FraudScoreRequest.builder()
                .transactionId(savedTransaction.getId())
                .merchantId(merchantId)
                .currency(savedTransaction.getCurrency())
                .customerEmail(savedTransaction.getCustomerEmail())
                .amount(savedTransaction.getAmount())
                .build());

        savedTransaction.setFraudDecision(fraudScoreResponse.decision());
        savedTransaction.setFraudScore(fraudScoreResponse.fraudScore());

        // 3. CHARGE/ HUMAN CHECK / BLOCK
        String paymentUrl = null;
        // TODO: make it transacitonal outbox instead of kafka direct publishing
        switch (fraudScoreResponse.decision()) {
            case BLOCK -> {
                savedTransaction.setStatus(TransactionStatus.BLOCKED);
                savedTransaction.setFailureReason("Blocked by fraud detection (score: " + fraudScoreResponse.fraudScore() + ")");
                transactionRepository.save(savedTransaction);
                paymentEventPublisher.publishPaymentFailed(savedTransaction, "fraud_blocked");
                log.warn("Payment BLOCKED: {} (fraud score: {})", savedTransaction.getId(), fraudScoreResponse.fraudScore());
                throw new PaymentBlockedException(savedTransaction.getId().toString(), fraudScoreResponse.fraudScore());
            }
            case REVIEW -> {
                savedTransaction.setStatus(TransactionStatus.REVIEW);
                transactionRepository.save(savedTransaction);
                log.info("Payment sent to REVIEW: {} (fraud score: {})", savedTransaction.getId(), fraudScoreResponse.fraudScore());
            }
            case APPROVE -> {
                paymentUrl = yooKassaService.chargeViaYooKassa(request);
                paymentEventPublisher.publishPaymentCreated(savedTransaction);
            }
        }


        return TransactionResponse.builder()
                .amount(request.amount())
                .customerEmail(request.customerEmail())
                .fraudDecision(fraudScoreResponse.decision())
                .paymentUrl(paymentUrl)
                .build();
    }
}
