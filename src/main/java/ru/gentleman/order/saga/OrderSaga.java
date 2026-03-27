package ru.gentleman.order.saga;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.gentleman.common.command.ActivateSubscriptionCommand;
import ru.gentleman.common.command.CreateCryptoPaymentCommand;
import ru.gentleman.common.command.RejectOrderCommand;
import ru.gentleman.common.command.RenewSubscriptionCommand;
import ru.gentleman.common.dto.OrderType;
import ru.gentleman.common.event.CryptoPaymentCompletedEvent;
import ru.gentleman.common.event.OrderCompletedEvent;
import ru.gentleman.common.event.OrderCreatedEvent;
import ru.gentleman.common.query.SubscriptionExistsQuery;
import ru.gentleman.order.command.CompleteOrderCommand;
import ru.gentleman.order.command.RollbackCreateOrderCommand;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Saga
@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderSaga {

    @JsonIgnore
    @Autowired
    private transient CommandGateway commandGateway;

    @JsonIgnore
    @Autowired
    private transient QueryGateway queryGateway;

    @JsonIgnore
    @Value("${url.callback}")
    private transient String callBackUrl;

    @JsonIgnore
    @Value("${url.successUrl}")
    private transient String successUrl;

    @JsonIgnore
    @Value("${url.cancelUrl}")
    private transient String cancelUrl;

    private UUID userId;

    private UUID courseId;

    private Integer days;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(OrderCreatedEvent event) {
        log.info("Saga Event 1 [Start] : Received OrderCreatedEvent {}", event);

        SagaLifecycle.associateWith("orderId", event.id().toString());

        this.userId = event.userId();
        this.courseId = event.courseId();
        this.days = event.subscriptionPeriodDays();

        if(event.type() == OrderType.CRYPTO) {
            CreateCryptoPaymentCommand command = CreateCryptoPaymentCommand.builder()
                    .id(UUID.randomUUID())
                    .orderId(event.id())
                    .priceAmount(event.totalAmount())
                    .priceCurrency(event.currency())
                    .ipnCallbackUrl(callBackUrl)
                    .successUrl(successUrl)
                    .cancelUrl(cancelUrl)
                    .orderDescription(event.title())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            commandGateway.send(command, new CommandCallback<>() {
                @Override
                public void onResult(@Nonnull CommandMessage<? extends CreateCryptoPaymentCommand> commandMessage,
                                     @Nonnull CommandResultMessage<?> commandResultMessage) {
                    if(commandResultMessage.isExceptional()) {
                        Throwable cause = commandResultMessage.exceptionResult();
                        String errorMessage = (cause != null) ? cause.getMessage() : "Unknown error";

                        log.error("Saga Event 1 Error : Rollback OrderCreatedEvent: {}, " +
                                "error message - {}", event.id(), errorMessage);

                        RollbackCreateOrderCommand rollbackCommand =
                                new RollbackCreateOrderCommand(event.id(), errorMessage);

                        commandGateway.send(rollbackCommand);
                    }
                }
            });
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(CryptoPaymentCompletedEvent event) {
        log.info("Saga Event 2 : Received CryptoPaymentCompletedEvent: {}", event);

        CompleteOrderCommand command = new CompleteOrderCommand(event.orderId(), event.cryptoPaymentStatus());

        commandGateway.send(command, new CommandCallback<>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends CompleteOrderCommand> commandMessage,
                                 @Nonnull CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()) {
                    Throwable cause = commandResultMessage.exceptionResult();
                    String errorMessage = (cause != null) ? cause.getMessage() : "Unknown error";

                    log.error("!!! CRITICAL ERROR !!! Payment received for Order {}, " +
                                    "but status update FAILED. Error message {}",
                            event.orderId(), errorMessage, cause);
                }
            }
        });
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(OrderCompletedEvent event) {
        log.info("Saga Event 3 [End] : Received OrderCompletedEvent: {}", event);

        String combinedIdSource = userId.toString() + "|" + courseId.toString();
        UUID subscriptionId = UUID.nameUUIDFromBytes(combinedIdSource.getBytes(StandardCharsets.UTF_8));

        queryGateway.query(new SubscriptionExistsQuery(subscriptionId), ResponseTypes.instanceOf(Boolean.class))
                .thenAccept(queryIdExists -> {
                    if (!queryIdExists) {
                        ActivateSubscriptionCommand command = ActivateSubscriptionCommand.builder()
                                .id(subscriptionId)
                                .courseId(courseId)
                                .createdAt(Instant.now())
                                .days(days)
                                .isActive(true)
                                .orderId(event.id())
                                .userId(userId)
                                .build();

                        commandGateway.send(command, (commandMessage, commandResultMessage) -> {
                            if (commandResultMessage.isExceptional()) {
                                Throwable cause = commandResultMessage.exceptionResult();
                                String errorMessage = (cause != null) ? cause.getMessage() : "Unknown error";

                                log.error("Saga Event 3 ActivateSubscriptionCommand Error : Rollback OrderCompletedEvent: {}, " +
                                        "error message - {}", event.id(), errorMessage);

                                RejectOrderCommand rollbackCommand =
                                        new RejectOrderCommand(event.id(), errorMessage);

                                commandGateway.send(rollbackCommand);
                            }
                        });
                    } else {
                        RenewSubscriptionCommand command = new RenewSubscriptionCommand(subscriptionId, event.id(), days);

                        commandGateway.send(command, new CommandCallback<>() {
                            @Override
                            public void onResult(@Nonnull CommandMessage<? extends RenewSubscriptionCommand> commandMessage,
                                                 @Nonnull CommandResultMessage<?> commandResultMessage) {
                                if (commandResultMessage.isExceptional()) {
                                    Throwable cause = commandResultMessage.exceptionResult();
                                    String errorMessage = (cause != null) ? cause.getMessage() : "Unknown error";

                                    log.error("Saga Event 3 RenewSubscriptionCommand Error : Rollback OrderCompletedEvent: {}, " +
                                            "error message - {}", event.id(), errorMessage);

                                    RejectOrderCommand rollbackCommand =
                                            new RejectOrderCommand(event.id(), errorMessage);

                                    commandGateway.send(rollbackCommand);
                                }
                            }
                        });
                    }
                }).exceptionally(e -> {
                    log.error("Rolling back order {} due to:", event.id(), e);

                    commandGateway.send(new RejectOrderCommand(event.id(), e.getMessage()));
                    return null;
                });
    }
}
