package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.DeliverySlotDTO;
import com.ooadproject.backend.entities.DeliverySlot;
import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.repositories.DeliverySlotRepository;
import com.ooadproject.backend.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryService {

    @Autowired
    private DeliverySlotRepository deliverySlotRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<DeliverySlotDTO> getAllDeliverySlots() {
        return deliverySlotRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DeliverySlotDTO> getDeliverySlotsByDate(LocalDate date) {
        return deliverySlotRepository.findByDeliveryDate(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DeliverySlotDTO> getDeliverySlotsByStatus(DeliverySlot.DeliveryStatus status) {
        return deliverySlotRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DeliverySlotDTO> getDeliverySlotsByDateRange(LocalDate startDate, LocalDate endDate) {
        return deliverySlotRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeliverySlotDTO createDeliverySlot(DeliverySlotDTO deliverySlotDTO) {
        // Verify the order exists
        Order order = orderRepository.findById(deliverySlotDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + deliverySlotDTO.getOrderId()));

        DeliverySlot deliverySlot = new DeliverySlot();
        deliverySlot.setOrder(order);
        deliverySlot.setDeliveryDate(deliverySlotDTO.getDeliveryDate());
        deliverySlot.setTimeSlot(deliverySlotDTO.getTimeSlot());
        deliverySlot.setCourierName(deliverySlotDTO.getCourierName());
        deliverySlot.setStatus(DeliverySlot.DeliveryStatus.PENDING);

        DeliverySlot saved = deliverySlotRepository.save(deliverySlot);
        return convertToDTO(saved);
    }

    public DeliverySlotDTO updateDeliveryStatus(Integer slotId, DeliverySlot.DeliveryStatus status) {
        DeliverySlot deliverySlot = deliverySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Delivery slot not found: " + slotId));

        deliverySlot.setStatus(status);

        // If delivered, also update the order status
        if (status == DeliverySlot.DeliveryStatus.DELIVERED) {
            Order order = deliverySlot.getOrder();
            order.setStatus(Order.OrderStatus.Delivered);
            orderRepository.save(order);
        }

        DeliverySlot updated = deliverySlotRepository.save(deliverySlot);
        return convertToDTO(updated);
    }

    public DeliverySlotDTO assignCourier(Integer slotId, String courierName) {
        DeliverySlot deliverySlot = deliverySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Delivery slot not found: " + slotId));

        deliverySlot.setCourierName(courierName);
        deliverySlot.setStatus(DeliverySlot.DeliveryStatus.ASSIGNED);
        DeliverySlot updated = deliverySlotRepository.save(deliverySlot);
        return convertToDTO(updated);
    }

    public void deleteDeliverySlot(Integer slotId) {
        deliverySlotRepository.deleteById(slotId);
    }

    private DeliverySlotDTO convertToDTO(DeliverySlot deliverySlot) {
        DeliverySlotDTO dto = new DeliverySlotDTO();
        dto.setSlotId(deliverySlot.getSlotId());
        dto.setDeliveryDate(deliverySlot.getDeliveryDate());
        dto.setTimeSlot(deliverySlot.getTimeSlot());
        dto.setCourierName(deliverySlot.getCourierName());
        dto.setStatus(deliverySlot.getStatus());

        if (deliverySlot.getOrder() != null) {
            dto.setOrderId(deliverySlot.getOrder().getOrderId());
            dto.setDeliveryAddress(deliverySlot.getOrder().getDeliveryAddress());
            dto.setContactNumber(deliverySlot.getOrder().getContactNumber());
            if (deliverySlot.getOrder().getUser() != null) {
                dto.setCustomerName(deliverySlot.getOrder().getUser().getUsername());
            }
        }

        return dto;
    }
}