package ru.gentleman.order.dto;

import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.order.entity.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID userId,
        String title,
        String currency,
        BigDecimal totalAmount,
        OrderType orderType,
        OrderStatus orderStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
