package com.ooadproject.backend.repositories;

import com.ooadproject.backend.entities.DeliverySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliverySlotRepository extends JpaRepository<DeliverySlot, Integer> {

    List<DeliverySlot> findByDeliveryDate(LocalDate deliveryDate);

    List<DeliverySlot> findByStatus(DeliverySlot.DeliveryStatus status);

    List<DeliverySlot> findByCourierName(String courierName);

    @Query("SELECT ds FROM DeliverySlot ds WHERE ds.deliveryDate BETWEEN :startDate AND :endDate")
    List<DeliverySlot> findByDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT ds FROM DeliverySlot ds JOIN ds.order o WHERE o.user.userId = :userId")
    List<DeliverySlot> findByUserId(@Param("userId") Integer userId);
}