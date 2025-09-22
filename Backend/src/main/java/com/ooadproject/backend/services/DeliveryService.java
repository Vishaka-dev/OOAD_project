package com.ooadproject.backend.service;

import com.ooadproject.backend.dto.DeliverySlotDTO;
import com.ooadproject.backend.entity.DeliverySlot;
import com.ooadproject.backend.entity.DeliverySlot.DeliveryStatus;
import com.ooadproject.backend.repository.DeliverySlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryService {

    @Autowired
    private DeliverySlotRepository deliverySlotRepository;

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

    public List<DeliverySlotDTO> getDeliverySlotsByStatus(DeliveryStatus status) {
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
        DeliverySlot deliverySlot = new DeliverySlot();
        deliverySlot.setOrderId(deliverySlotDTO.getOrderId());
        deliverySlot.setDeliveryDate(deliverySlotDTO.getDeliveryDate());
        deliverySlot.setTimeSlot(deliverySlotDTO.getTimeSlot());
        deliverySlot.setCourierName(deliverySlotDTO.getCourierName());
        deliverySlot.setStatus(DeliveryStatus.PENDING);

        DeliverySlot saved = deliverySlotRepository.save(deliverySlot);
        return convertToDTO(saved);
    }

    public DeliverySlotDTO updateDeliveryStatus(Integer slotId, DeliveryStatus status) {
        DeliverySlot deliverySlot = deliverySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Delivery slot not found: " + slotId));

        deliverySlot.setStatus(status);
        DeliverySlot updated = deliverySlotRepository.save(deliverySlot);
        return convertToDTO(updated);
    }

    public DeliverySlotDTO assignCourier(Integer slotId, String courierName) {
        DeliverySlot deliverySlot = deliverySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Delivery slot not found: " + slotId));

        deliverySlot.setCourierName(courierName);
        deliverySlot.setStatus(DeliveryStatus.ASSIGNED);
        DeliverySlot updated = deliverySlotRepository.save(deliverySlot);
        return convertToDTO(updated);
    }

    public void deleteDeliverySlot(Integer slotId) {
        deliverySlotRepository.deleteById(slotId);
    }

    private DeliverySlotDTO convertToDTO(DeliverySlot deliverySlot) {
        DeliverySlotDTO dto = new DeliverySlotDTO();
        dto.setSlotId(deliverySlot.getSlotId());
        dto.setOrderId(deliverySlot.getOrderId());
        dto.setDeliveryDate(deliverySlot.getDeliveryDate());
        dto.setTimeSlot(deliverySlot.getTimeSlot());
        dto.setCourierName(deliverySlot.getCourierName());
        dto.setStatus(deliverySlot.getStatus());

        if (deliverySlot.getOrder() != null) {
            dto.setDeliveryAddress(deliverySlot.getOrder().getDeliveryAddress());
            dto.setContactNumber(deliverySlot.getOrder().getContactNumber());
            // Assuming you have user relationship in Order
            // dto.setCustomerName(deliverySlot.getOrder().getUser().getUsername());
        }

        return dto;
    }
}
