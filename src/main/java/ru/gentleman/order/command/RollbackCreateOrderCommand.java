package ru.gentleman.order.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record RollbackCreateOrderCommand(
        @TargetAggregateIdentifier
        UUID id
) {
}
