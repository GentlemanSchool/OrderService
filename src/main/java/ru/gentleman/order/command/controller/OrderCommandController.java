package ru.gentleman.order.command.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.order.command.CreateOrderCommand;
import ru.gentleman.order.command.DeleteOrderCommand;
import ru.gentleman.order.dto.OrderDto;

import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCommandController {

    private final CommandGateway commandGateway;

    private final MessageSource messageSource;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid OrderDto orderDto,
                                         BindingResult bindingResult) {

        UUID id = UUID.randomUUID();
        CreateOrderCommand command = CreateOrderCommand.builder()
                .id(id)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .currency(orderDto.currency())
                .subscriptionPeriodDays(orderDto.days())
                .courseId(orderDto.courseId())
                .status(OrderStatus.CREATED)
                .title(orderDto.title())
                .totalAmount(orderDto.totalAmount())
                .type(orderDto.type())
                .userId(orderDto.userId())
                .build();

        this.commandGateway.sendAndWait(command);

        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + id))
                .body(
                        this.messageSource.getMessage(
                                "info.order.created",
                                new Object[]{id},
                                Locale.getDefault()
                        )
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        DeleteOrderCommand command = new DeleteOrderCommand(id);

        this.commandGateway.sendAndWait(command);

        return ResponseEntity.noContent().build();
    }
}
