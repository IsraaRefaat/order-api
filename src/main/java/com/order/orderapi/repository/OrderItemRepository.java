package com.order.orderapi.repository;

import com.order.orderapi.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.customerEmail = :customerEmail")
    List<OrderItem> findByCustomerEmail(@Param("customerEmail") String customerEmail);

    void deleteByOrderId(Long orderId);
}