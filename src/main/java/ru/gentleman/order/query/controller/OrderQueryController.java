package ru.gentleman.order.query.controller;

import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.*;
import ru.gentleman.common.util.ExceptionUtils;
import ru.gentleman.order.dto.OrderDto;
import ru.gentleman.order.query.FindAllOrdersByUserIdQuery;
import ru.gentleman.order.query.FindOrderByIdQuery;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final QueryGateway queryGateway;

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable("id") UUID id) {
        OrderDto orderDto = this.queryGateway.query(new FindOrderByIdQuery(id),
                ResponseTypes.instanceOf(OrderDto.class)).join();

        if(orderDto == null) {
            throw ExceptionUtils.notFound("error.order.not_found", id);
        }

        return orderDto;
    }

    @GetMapping(params = "userId")
    public List<Object> getByUserId(@RequestParam("userId") UUID userId) {
        return this.queryGateway.query(new FindAllOrdersByUserIdQuery(userId),
                ResponseTypes.multipleInstancesOf(Object.class)).join();
    }
}
