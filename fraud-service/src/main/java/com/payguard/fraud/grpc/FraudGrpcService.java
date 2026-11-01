package com.payguard.fraud.grpc;

import com.nibavs.payguard.proto.FraudScoreProtoRequest;
import com.nibavs.payguard.proto.FraudScoreProtoResponse;
import com.nibavs.payguard.proto.FraudServiceGrpc;
import com.payguard.fraud.service.FraudScoringOrchestrator;
import com.payguard.shared.fraud.FraudScoreResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class FraudGrpcService extends FraudServiceGrpc.FraudServiceImplBase {

    private final FraudScoringOrchestrator fraudScoringOrchestrator;

    @Override
    public void scoreTransaction(FraudScoreProtoRequest request, StreamObserver<FraudScoreProtoResponse> responseObserver) {
        try {
            log.info("gRPC scoring request for transaction: {}", request.getTransactionId());
            FraudScoreProtoResponse response = fraudScoringOrchestrator.scoreTransaction(request);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC scoring failed for transaction {}: {}",
                    request.getTransactionId(), e.getMessage());
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Fraud scoring failed: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}
