package ru.gentleman.order.command;

import java.util.UUID;

public record DeleteOrderCommand(
        UUID id
) {
}
