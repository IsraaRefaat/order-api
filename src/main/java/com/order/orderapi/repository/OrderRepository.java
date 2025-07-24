package com.order.orderapi.repository;

import com.order.orderapi.entity.Order;
import com.order.orderapi.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmail(String customerEmail);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmailAndStatus(String customerEmail, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    Optional<Order> findByIdAndCustomerEmail(Long id, String customerEmail);
}