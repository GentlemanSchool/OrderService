package ru.gentleman.order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gentleman.common.dto.OrderStatus;
import ru.gentleman.common.util.ExceptionUtils;
import ru.gentleman.order.cache.CacheClear;
import ru.gentleman.order.dto.OrderDto;
import ru.gentleman.order.entity.Order;
import ru.gentleman.order.mapper.OrderMapper;
import ru.gentleman.order.repository.OrderRepository;
import ru.gentleman.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultOrderService implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final CacheClear cacheClear;

    @Override
    @Cacheable(value = "order", key = "#id")
    public OrderDto get(UUID id) {
        log.info("get {}", id);

        Order order = this.orderRepository.findByIdAndIsActive(id, true)
                .orElse(null);

        return this.orderMapper.toDto(order);
    }

    @Override
    @Cacheable(value = "allOrdersByUserId", key = "#userId")
    public List<OrderDto> getAllByUserId(UUID userId) {
        log.info("getAllByUserId {}", userId);

        return this.orderMapper.toDto(
                this.orderRepository.findAllByUserIdAndIsActive(userId, true)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "allOrdersByUserId", key = "#orderDto.userId()")
    public void create(OrderDto orderDto) {
        log.info("create {}", orderDto);

        Order mappedOrder = this.orderMapper.toEntity(orderDto);
        mappedOrder.setIsActive(true);

        this.orderRepository.save(mappedOrder);
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "#id")
    public void updateStatus(UUID id, OrderStatus orderStatus) {
        log.info("updateStatus {} {}", id, orderStatus);

        Order order = this.orderRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> ExceptionUtils.notFound("error.order.not_found", id));

        order.setStatus(orderStatus);

        this.cacheClear.clearAllOrdersByUserId(order.getUserId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "order", key = "#id")
    public void delete(UUID id) {
        log.info("delete {}", id);

        Order order = this.orderRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> ExceptionUtils.notFound("error.order.not_found", id));

        order.setIsActive(false);

        this.cacheClear.clearAllOrdersByUserId(order.getUserId());
    }

    @Override
    public boolean existsById(UUID id) {
        log.info("existsById {}", id);

        return this.orderRepository.existsById(id);
    }
}
