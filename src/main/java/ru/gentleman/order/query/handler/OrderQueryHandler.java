package ru.gentleman.order.query.handler;

import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;
import ru.gentleman.order.dto.OrderDto;
import ru.gentleman.order.query.FindAllOrdersByUserIdQuery;
import ru.gentleman.order.query.FindOrderByIdQuery;
import ru.gentleman.order.service.OrderService;

import java.util.List;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"unused"})
public class OrderQueryHandler {

    private final OrderService orderService;

    @QueryHandler
    public OrderDto on(FindOrderByIdQuery query) {
        return this.orderService.get(query.id());
    }

    @QueryHandler
    public List<OrderDto> on(FindAllOrdersByUserIdQuery query) {
        return this.orderService.getAllByUserId(query.userId());
    }
}
