package ru.gentleman.order.mapper;

import org.mapstruct.Mapper;
import ru.gentleman.order.dto.OrderDto;
import ru.gentleman.order.entity.Order;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDto toDto(Order order);

    Order toEntity(OrderDto orderDto);

    List<OrderDto> toDto(List<Order> entities);
}
