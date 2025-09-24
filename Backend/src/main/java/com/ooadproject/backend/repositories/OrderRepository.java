package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByStatus(Order.OrderStatus status);

    Long countByStatus(Order.OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findTop10ByOrderByOrderDateDesc();

    List<Order> findByUserId(Integer userId);

    List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    // ✅ Add this method for paging
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
}
