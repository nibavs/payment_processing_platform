package com.payguard.payment.service;

import com.nibavs.payguard.proto.FraudScoreProtoRequest;
import com.nibavs.payguard.proto.FraudScoreProtoResponse;
import com.payguard.payment.client.FraudClient;
import com.payguard.payment.dto.ChargeRequest;
import com.payguard.payment.dto.TransactionResponse;
import com.payguard.payment.entity.Transaction;
import com.payguard.payment.mapper.TransactionMapper;
import com.payguard.payment.repository.TransactionRepository;
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
    private final PaymentEventPublisher paymentEventPublisher;
    private final TransactionMapper transactionMapper;
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

        // 2 Fraud-check grpc request
        FraudScoreProtoResponse fraudScoreResponse = fraudClient.score(FraudScoreProtoRequest.newBuilder()
                .setTransactionId(savedTransaction.getId().toString())
                .setMerchantId(merchantId.toString())
                .setCurrency(savedTransaction.getCurrency())
                .setCustomerEmail(savedTransaction.getCustomerEmail())
                .setAmount(savedTransaction.getAmount().toString())
                .setMerchantCategory("merchantCategory")
                .setCountry("Russia")
                .setTimestamp(System.currentTimeMillis() / 1000)
                .build());

        savedTransaction.setFraudDecision(fraudScoreResponse.getDecision());
        savedTransaction.setFraudScore(fraudScoreResponse.getFraudScore());

        // 3. CHARGE/ HUMAN CHECK / BLOCK
        String paymentUrl = null;
        // TODO: make it transacitonal outbox instead of kafka direct publishing
        switch (fraudScoreResponse.getDecision()) {
            case BLOCK -> {
                savedTransaction.setStatus(TransactionStatus.BLOCKED);
                savedTransaction.setFailureReason("Blocked by fraud detection (score: " + fraudScoreResponse.getFraudScore() + ")");
                transactionRepository.save(savedTransaction);
                paymentEventPublisher.publishPaymentFailed(savedTransaction, "fraud_blocked");
                log.warn("Payment BLOCKED: {} (fraud score: {})", savedTransaction.getId(), fraudScoreResponse.getFraudScore());
            }
            case REVIEW -> {
                savedTransaction.setStatus(TransactionStatus.REVIEW);
                transactionRepository.save(savedTransaction);
                log.info("Payment sent to REVIEW: {} (fraud score: {})", savedTransaction.getId(), fraudScoreResponse.getFraudScore());
            }
            // TODO: get rid of rest request here
            case APPROVE -> {
                paymentUrl = yooKassaService.chargeViaYooKassa(request, savedTransaction);
                paymentEventPublisher.publishPaymentCreated(savedTransaction);
            }
        }
        return transactionMapper.toResponse(savedTransaction, paymentUrl);
    }
}
