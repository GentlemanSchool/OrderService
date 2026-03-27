package ru.gentleman.order.query.projection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.common.event.*;
import ru.gentleman.order.dto.OrderDto;
import ru.gentleman.order.service.OrderService;

@Slf4j
@Component
@ProcessingGroup("order-group")
@RequiredArgsConstructor
@SuppressWarnings({"unused"})
public class OrderProjection {

    private final OrderService orderService;

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderDto orderDto = OrderDto.builder()
                .id(event.id())
                .createdAt(event.createdAt())
                .currency(event.currency())
                .days(event.subscriptionPeriodDays())
                .courseId(event.courseId())
                .status(event.status())
                .title(event.title())
                .totalAmount(event.totalAmount())
                .type(event.type())
                .updatedAt(event.updatedAt())
                .userId(event.userId())
                .build();

        this.orderService.create(orderDto);
    }

    @EventHandler
    public void on(RollbackCreateOrderEvent event) {
        this.orderService.updateStatus(event.id(), OrderStatus.FAILED);
    }

    @EventHandler
    public void on(OrderCompletedEvent event) {
        this.orderService.updateStatus(event.id(), event.cryptoPaymentStatus().toOrderStatus());
    }

    @EventHandler
    public void on(OrderDeletedEvent event) {
        this.orderService.delete(event.id());
    }

    @EventHandler
    public void on(OrderRejectedEvent event) {
        this.orderService.updateStatus(event.id(), OrderStatus.FAILED);
    }
}
