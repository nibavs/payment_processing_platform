package com.payguard.payment.service;

import com.payguard.payment.dto.ChargeRequest;
import com.payguard.payment.dto.TransactionResponse;
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

    public TransactionResponse processPayment(UUID merchantId, @Valid ChargeRequest request) {

        log.info("Transaction created. Amount {}, merchantId {}, customerEmail {}", request.amount(), merchantId, request.customerEmail());
        String paymentUrl = yooKassaService.chargeViaYooKassa(request);

        return TransactionResponse.builder()
                .amount(request.amount())
                .customerEmail(request.customerEmail())
                .paymentUrl(paymentUrl)
                .build();
    }
}
