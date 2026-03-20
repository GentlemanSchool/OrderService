package ru.gentleman.order.service;

import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.order.dto.OrderDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderDto get(UUID id);

    List<OrderDto> getAllByUserId(UUID userId);

    void create(OrderDto orderDto);

    void updateStatus(UUID id, OrderStatus orderStatus);

    void delete(UUID id);
}
