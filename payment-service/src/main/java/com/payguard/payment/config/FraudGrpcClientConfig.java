package com.payguard.payment.config;

import com.nibavs.payguard.proto.FraudServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration(proxyBeanMethods = false)
public class FraudGrpcClientConfig {

    @Bean
    FraudServiceGrpc.FraudServiceBlockingStub fraudCheckStub(GrpcChannelFactory channels) {
        return FraudServiceGrpc.newBlockingStub(channels.createChannel("fraud-server"));
    }
}
