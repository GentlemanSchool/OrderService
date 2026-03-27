package ru.gentleman.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gentleman.order.entity.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndIsActive(UUID id, Boolean isActive);

    List<Order> findAllByUserIdAndIsActive(UUID userId, Boolean isActive);
}
