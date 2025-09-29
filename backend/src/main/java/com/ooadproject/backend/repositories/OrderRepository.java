package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2")
    List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}