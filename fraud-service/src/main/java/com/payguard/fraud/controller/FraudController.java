package com.payguard.fraud.controller;

import com.payguard.fraud.service.FraudScoringOrchestrator;
import com.payguard.shared.fraud.FraudScoreRequest;
import com.payguard.shared.fraud.FraudScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
class FraudController {


    private final FraudScoringOrchestrator fraudScoringOrchestrator;

    @PostMapping("/score")
    public ResponseEntity<FraudScoreResponse> scoreTransaction(@RequestBody FraudScoreRequest request) {
        log.info("Fraud score request {}", request);

        FraudScoreResponse result = fraudScoringOrchestrator.scoreTransaction(request);
        return ResponseEntity.ok(result);
    }
}
