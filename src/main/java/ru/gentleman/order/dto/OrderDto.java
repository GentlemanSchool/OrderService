package ru.gentleman.order.dto;

import lombok.Builder;
import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.common.dto.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderDto(
        UUID id,
        UUID userId,
        UUID courseId,
        String title,
        String currency,
        BigDecimal totalAmount,
        OrderType type,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt,
        Integer days
) {
}
