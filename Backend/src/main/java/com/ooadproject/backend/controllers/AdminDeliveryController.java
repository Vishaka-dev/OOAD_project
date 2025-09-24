package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.DeliverySlotDTO;
import com.ooadproject.backend.entities.DeliverySlot;
import com.ooadproject.backend.services.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/delivery")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminDeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<DeliverySlotDTO>> getAllDeliverySlots() {
        List<DeliverySlotDTO> slots = deliveryService.getAllDeliverySlots();
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<DeliverySlotDTO>> getDeliverySlotsByDate(@PathVariable String date) {
        try {
            LocalDate deliveryDate = LocalDate.parse(date);
            List<DeliverySlotDTO> slots = deliveryService.getDeliverySlotsByDate(deliveryDate);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliverySlotDTO>> getDeliverySlotsByStatus(@PathVariable DeliverySlot.DeliveryStatus status) {
        List<DeliverySlotDTO> slots = deliveryService.getDeliverySlotsByStatus(status);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<DeliverySlotDTO>> getDeliverySlotsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<DeliverySlotDTO> slots = deliveryService.getDeliverySlotsByDateRange(start, end);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createDeliverySlot(@RequestBody DeliverySlotDTO deliverySlotDTO) {
        try {
            DeliverySlotDTO created = deliveryService.createDeliverySlot(deliverySlotDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Delivery slot created successfully",
                    "data", created
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{slotId}/status")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable Integer slotId,
            @RequestParam DeliverySlot.DeliveryStatus status) {
        try {
            DeliverySlotDTO updated = deliveryService.updateDeliveryStatus(slotId, status);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Delivery status updated successfully",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{slotId}/courier")
    public ResponseEntity<?> assignCourier(
            @PathVariable Integer slotId,
            @RequestParam String courierName) {
        try {
            DeliverySlotDTO updated = deliveryService.assignCourier(slotId, courierName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Courier assigned successfully",
                    "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteDeliverySlot(@PathVariable Integer slotId) {
        try {
            deliveryService.deleteDeliverySlot(slotId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Delivery slot deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}