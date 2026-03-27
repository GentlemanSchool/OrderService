package ru.gentleman.order.command.interseptor;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;
import ru.gentleman.common.util.ExceptionUtils;
import ru.gentleman.order.command.CompleteOrderCommand;
import ru.gentleman.order.command.DeleteOrderCommand;
import ru.gentleman.order.command.RollbackCreateOrderCommand;
import ru.gentleman.order.service.OrderService;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class OrderInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private final OrderService orderService;

    @Nonnull
    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(@Nonnull List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {
            if(CompleteOrderCommand.class.equals(command.getPayloadType())) {
                CompleteOrderCommand completeOrderCommand = (CompleteOrderCommand) command.getPayload();

                if(!this.orderService.existsById(completeOrderCommand.id())){
                    ExceptionUtils.notFound("error.order.not_found", completeOrderCommand.id());
                }
            } else if (DeleteOrderCommand.class.equals(command.getPayloadType())) {
                DeleteOrderCommand deleteOrderCommand = (DeleteOrderCommand) command.getPayload();

                if(!this.orderService.existsById(deleteOrderCommand.id())){
                    ExceptionUtils.notFound("error.order.not_found", deleteOrderCommand.id());
                }
            } else if (RollbackCreateOrderCommand.class.equals(command.getPayloadType())) {
                RollbackCreateOrderCommand createQuestionCommand = (RollbackCreateOrderCommand) command.getPayload();

                if(!this.orderService.existsById(createQuestionCommand.id())){
                    ExceptionUtils.notFound("error.order.not_found", createQuestionCommand.id());
                }
            }

            return command;
        };
    }
}
