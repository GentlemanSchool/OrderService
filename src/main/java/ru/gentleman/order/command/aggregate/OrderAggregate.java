package ru.gentleman.order.command.aggregate;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.common.dto.OrderType;
import ru.gentleman.common.event.OrderCompletedEvent;
import ru.gentleman.common.event.OrderCreatedEvent;
import ru.gentleman.common.event.RollbackCreateOrderEvent;
import ru.gentleman.order.command.CompleteOrderCommand;
import ru.gentleman.order.command.CreateOrderCommand;
import ru.gentleman.order.command.RollbackCreateOrderCommand;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Aggregate
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class OrderAggregate {

    @AggregateIdentifier
    private UUID id;

    private UUID userId;

    private String title;

    private String currency;

    private BigDecimal totalAmount;

    private OrderType type;

    private OrderStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    public OrderAggregate() {

    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .id(command.id())
                .createdAt(command.createdAt())
                .currency(command.currency())
                .status(command.status())
                .title(command.title())
                .totalAmount(command.totalAmount())
                .type(command.type())
                .updatedAt(command.updatedAt())
                .userId(command.userId())
                .build();

        AggregateLifecycle.apply(event);
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        this.id = event.id();
        this.userId = event.userId();
        this.title = event.title();
        this.currency = event.currency();
        this.totalAmount = event.totalAmount();
        this.type = event.type();
        this.status = event.status();
        this.createdAt = event.createdAt();
        this.updatedAt = event.updatedAt();
    }

    @CommandHandler
    public void handle(RollbackCreateOrderCommand command) {
        if(status == OrderStatus.FAILED) {
            throw new CommandExecutionException(
                    "error.order.already_failed",
                    null,
                    command.id()
            );
        }

        RollbackCreateOrderEvent event = new RollbackCreateOrderEvent(id);

        AggregateLifecycle.apply(event);
    }

    @EventHandler
    public void on(RollbackCreateOrderEvent event) {
        this.status = OrderStatus.FAILED;
    }

    @CommandHandler
    public void handle(CompleteOrderCommand command) {
        if(status == OrderStatus.FAILED || status == OrderStatus.SUCCESSFUL
        || status == OrderStatus.EXPIRED) {
            throw new CommandExecutionException(
                    "error.order.already_completed",
                    null,
                    command.id()
            );
        }

        OrderCompletedEvent event = new OrderCompletedEvent(command.id(),
                command.cryptoPaymentStatus());

        AggregateLifecycle.apply(event);
    }

    @EventHandler
    public void on(OrderCompletedEvent event) {
        this.status = event.cryptoPaymentStatus().toOrderStatus();
    }
}
