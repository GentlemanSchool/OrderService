package ru.gentleman.order.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.common.dto.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateOrderCommand(
        @TargetAggregateIdentifier
        UUID id,
        UUID userId,
        String title,
        String currency,
        BigDecimal totalAmount,
        OrderType type,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
