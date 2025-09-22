package com.ooadproject.backend.repository;

import com.ooadproject.backend.entity.Order;
import com.ooadproject.backend.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByStatus(OrderStatus status);

    Long countByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findTop10ByOrderByOrderDateDesc();

    List<Order> findByUserId(Integer userId);
}
