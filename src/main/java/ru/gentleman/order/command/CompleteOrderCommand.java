package ru.gentleman.order.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import ru.gentleman.common.dto.CryptoPaymentStatus;

import java.util.UUID;

public record CompleteOrderCommand(
        @TargetAggregateIdentifier
        UUID id,
        CryptoPaymentStatus cryptoPaymentStatus
) {
}
