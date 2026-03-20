package ru.gentleman.order.saga;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.gentleman.common.command.CreateCryptoPaymentCommand;
import ru.gentleman.common.dto.OrderType;
import ru.gentleman.common.event.CryptoPaymentCompletedEvent;
import ru.gentleman.common.event.OrderCreatedEvent;
import ru.gentleman.order.command.CompleteOrderCommand;
import ru.gentleman.order.command.RollbackCreateOrderCommand;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.UUID;

@Saga
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Value("${url.callback}")
    private transient String callBackUrl;

    @Value("${url.successUrl}")
    private transient String successUrl;

    @Value("${url.cancelUrl}")
    private transient String cancelUrl;


    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(OrderCreatedEvent event) {
        SagaLifecycle.associateWith("orderId", event.id().toString());

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

            this.commandGateway.send(command, new CommandCallback<>() {
                @Override
                public void onResult(@Nonnull CommandMessage<? extends CreateCryptoPaymentCommand> commandMessage,
                                     @Nonnull CommandResultMessage<?> commandResultMessage) {
                    if(commandResultMessage.isExceptional()) {
                        RollbackCreateOrderCommand rollbackCommand = new RollbackCreateOrderCommand(event.id());

                        commandGateway.send(rollbackCommand);
                    }
                }
            });
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(CryptoPaymentCompletedEvent event) {
        log.info("handle {}", event);
        CompleteOrderCommand command = new CompleteOrderCommand(event.orderId(), event.cryptoPaymentStatus());

        this.commandGateway.sendAndWait(command);
    }
}
