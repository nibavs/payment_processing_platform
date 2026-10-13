package com.payguard.payment.controller;

import com.payguard.payment.dto.ChargeRequest;
import com.payguard.payment.dto.TransactionResponse;
import com.payguard.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/charge")
    public ResponseEntity<TransactionResponse> charge(
            @RequestHeader("X-User-Id") UUID merchantId,
            @Valid @RequestBody ChargeRequest request
    ) {
        TransactionResponse response = paymentService.processPayment(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
