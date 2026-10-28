package com.payguard.payment.mapper;

import com.payguard.payment.dto.TransactionResponse;
import com.payguard.payment.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring"
)
public interface TransactionMapper {

    @Mapping(target = "paymentUrl", source = "paymentUrl")
    TransactionResponse toResponse(Transaction transaction, String paymentUrl);
}
