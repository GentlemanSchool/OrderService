package ru.gentleman.order.query;

import java.util.UUID;

public record FindAllOrdersByUserIdQuery(
        UUID userId
) {
}
