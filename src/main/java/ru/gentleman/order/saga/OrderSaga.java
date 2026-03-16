package ru.gentleman.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Value;
import ru.gentleman.common.command.CreateCryptoPaymentCommand;
import ru.gentleman.common.dto.OrderType;
import ru.gentleman.common.event.CryptoPaymentCompletedEvent;
import ru.gentleman.common.event.OrderCreatedEvent;
import ru.gentleman.common.event.RollbackCreateOrderEvent;
import ru.gentleman.order.command.CompleteOrderCommand;

import javax.annotation.Nonnull;

@Saga
@Slf4j
@RequiredArgsConstructor
public class OrderSaga {

    private final CommandGateway commandGateway;

    @Value("${url.callback}")
    private String callBackUrl;

    @Value("${url.successUrl}")
    private String successUrl;

    @Value("${url.cancelUrl}")
    private String cancelUrl;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(OrderCreatedEvent event) {
        if(event.type() == OrderType.CRYPTO) {
            CreateCryptoPaymentCommand command = CreateCryptoPaymentCommand.builder()
                    .orderId(event.id())
                    .priceAmount(event.totalAmount())
                    .priceCurrency(event.currency())
                    .ipnCallbackUrl(callBackUrl)
                    .successUrl(successUrl)
                    .cancelUrl(cancelUrl)
                    .orderDescription(event.title())
                    .build();

            this.commandGateway.send(command, new CommandCallback<>() {
                @Override
                public void onResult(@Nonnull CommandMessage<? extends CreateCryptoPaymentCommand> commandMessage,
                                     @Nonnull CommandResultMessage<?> commandResultMessage) {
                    if(commandResultMessage.isExceptional()) {
                        RollbackCreateOrderEvent rollbackCreateOrderEvent = new RollbackCreateOrderEvent(event.id());

                        commandGateway.send(rollbackCreateOrderEvent);
                    }
                }
            });
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(CryptoPaymentCompletedEvent event) {
        CompleteOrderCommand command = new CompleteOrderCommand(event.orderId(), event.cryptoPaymentStatus());

        this.commandGateway.sendAndWait(command);
    }
}
