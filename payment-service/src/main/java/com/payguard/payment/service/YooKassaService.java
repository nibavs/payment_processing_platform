package com.payguard.payment.service;

import com.payguard.payment.config.properties.YooKassaProperties;
import com.payguard.payment.dto.ChargeRequest;
import com.payguard.payment.entity.Transaction;
import com.payguard.payment.repository.TransactionRepository;
import com.payguard.shared.payment.TransactionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deelter.yookassa.YooKassa;
import ru.deelter.yookassa.model.*;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class YooKassaService {
    private final YooKassa yooKassaClient;
    private final YooKassaProperties yooKassaProperties;
    private final TransactionRepository transactionRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public String chargeViaYooKassa(ChargeRequest chargeRequest, Transaction transaction) {

        CreatePaymentRequest request = new CreatePaymentRequest()
                .setAmount(MonetaryAmount.of(chargeRequest.amount(), MonetaryAmount.CURRENCY_RUB))
                .setDescription(chargeRequest.customerEmail())
                .setCapture(true)
                .setConfirmation(new ConfirmationDataRedirect()
                        .setType(ConfirmationDataRedirect.TYPE_REDIRECT)
                        .setReturnUrl(yooKassaProperties.getReturnUrl())
                );

        String key = YooKassa.newIdempotenceKey();
        try {
            Payment payment = yooKassaClient.createPayment(request, key);
            transaction.setYooKassaPaymentId(payment.getId());

            return payment.getConfirmation().as(ConfirmationRedirect.class).getConfirmationUrl();
        } catch (Exception e) {
            log.error("YooKassa payment creation error occurred!\n{}", e.getMessage());

            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("YooKassa недоступна!");

            return "YooKassa Error";
        } finally {
            transactionRepository.save(transaction);
        }
    }


    @Transactional
    public void handlePaymentEventFromYooKassa(Payment payment) {
        Optional<Transaction> transactionOptional = transactionRepository.findByYooKassaPaymentId(payment.getId());

        if (transactionOptional.isEmpty()) {
            log.info("Платеж не из приложения {}, либо ошибка", payment.getId());
            return;
        }

        Transaction transaction = transactionOptional.get();

        // Idempotency
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("Платеж с ID: {}  уже обработан!", payment.getId());
            return;
        }

        // Outbox needed
        if (Payment.STATUS_SUCCEEDED.equals(payment.getStatus())) {
            log.info("Платеж с ID: {}  успешно прошел!", payment.getId());
            paymentEventPublisher.publishPaymentCompleted(transaction);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
        } else if (Payment.STATUS_CANCELED.equals(payment.getStatus())) {
            log.info("Платеж с ID: {}  отменен!", payment.getId());

            String failureReason = "Payment canceled";
            paymentEventPublisher.publishPaymentFailed(transaction, failureReason);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(failureReason);
            transactionRepository.save(transaction);
        }


    }
}
