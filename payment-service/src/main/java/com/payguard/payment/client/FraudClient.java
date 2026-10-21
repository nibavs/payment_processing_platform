package com.payguard.payment.client;

import com.payguard.shared.fraud.FraudScoreRequest;
import com.payguard.shared.fraud.FraudScoreResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FraudClient {

    private final RestClient restClient;

    public FraudClient(
            @Qualifier("fraudRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public FraudScoreResult score(FraudScoreRequest request) {
        FraudScoreResult result = restClient.post()
                .uri("/api/v1/fraud/score")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(FraudScoreResult.class);

        if (result == null) {
            throw new IllegalStateException("Fraud service returned empty response");
        }

        return result;
    }
}
