package ru.gentleman.order.command.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gentleman.order.command.CreateOrderCommand;
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
                .status(orderDto.status())
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
                                "",
                                new Object[]{id},
                                Locale.getDefault()
                        )
                );
    }
}
