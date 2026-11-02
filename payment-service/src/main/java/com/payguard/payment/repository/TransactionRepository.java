package com.payguard.payment.repository;

import com.payguard.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByYooKassaPaymentId(String paymentId);
}
