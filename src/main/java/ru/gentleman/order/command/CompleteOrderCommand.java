package ru.gentleman.order.command;

import ru.gentleman.common.dto.CryptoPaymentStatus;

import java.util.UUID;

public record CompleteOrderCommand(
        UUID id,
        CryptoPaymentStatus cryptoPaymentStatus
) {
}
