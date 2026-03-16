package ru.gentleman.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gentleman.order.entity.Order;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

}
