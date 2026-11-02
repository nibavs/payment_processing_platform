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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class PaymentService {
    private final YooKassaService yooKassaService;
    private final TransactionRepository transactionRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final TransactionMapper transactionMapper;
    private final FraudClient fraudClient;
    private final PaymentService self;

    public PaymentService(
            YooKassaService yooKassaService,
            TransactionRepository transactionRepository,
            PaymentEventPublisher paymentEventPublisher,
            TransactionMapper transactionMapper,
            FraudClient fraudClient,
            @Lazy PaymentService self
    ) {
        this.yooKassaService = yooKassaService;
        this.transactionRepository = transactionRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.transactionMapper = transactionMapper;
        this.fraudClient = fraudClient;
        this.self = self;
    }


    public TransactionResponse processPayment(UUID merchantId, @Valid ChargeRequest request) {
        // 1. Save new payment transaction with status pending
        Transaction savedTransaction = self.createPending(merchantId, request);

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
            case APPROVE -> {
                paymentUrl = yooKassaService.chargeViaYooKassa(request, savedTransaction);
                paymentEventPublisher.publishPaymentCreated(savedTransaction);
            }
        }
        return transactionMapper.toResponse(savedTransaction, paymentUrl);
    }

    @Transactional
    public Transaction createPending(UUID merchantId, ChargeRequest request) {
        Transaction transaction = Transaction.builder()
                .merchantId(merchantId)
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .customerEmail(request.customerEmail().toLowerCase().trim())
                .description(request.description())
                .metadata(request.metadata())
                .status(TransactionStatus.PENDING)
                .build();

        return transactionRepository.save(transaction);
    }
}
