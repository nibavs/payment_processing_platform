package com.payguard.payment.service;

import com.payguard.payment.config.properties.YooKassaProperties;
import com.payguard.payment.dto.ChargeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deelter.yookassa.YooKassa;
import ru.deelter.yookassa.model.*;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class YooKassaService {
    private final YooKassa yooKassaClient;
    private final YooKassaProperties yooKassaProperties;

    public String chargeViaYooKassa(ChargeRequest chargeRequest) {

        CreatePaymentRequest request = new CreatePaymentRequest()
                .setAmount(MonetaryAmount.of(chargeRequest.amount(), MonetaryAmount.CURRENCY_RUB))
                .setDescription(chargeRequest.customerEmail())
                .setCapture(false)
                .setConfirmation(new ConfirmationDataRedirect()
                        .setType(ConfirmationDataRedirect.TYPE_REDIRECT)
                        .setReturnUrl(yooKassaProperties.getReturnUrl())
                );

        String key = YooKassa.newIdempotenceKey();
        try {
            Payment payment = yooKassaClient.createPayment(request, key);
            return payment.getConfirmation().as(ConfirmationRedirect.class).getConfirmationUrl();
        } catch (Exception e) {
            log.error("YooKassa payment creation error occurred!\n{}", e.getMessage());
            return "YooKassa Error";
        }
    }

}
