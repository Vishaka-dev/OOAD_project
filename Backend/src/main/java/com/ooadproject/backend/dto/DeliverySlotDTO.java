package com.ooadproject.backend.dto;

import com.ooadproject.backend.entities.DeliverySlot.DeliveryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySlotDTO {
    private Integer slotId;
    private Integer orderId;
    private LocalDate deliveryDate;
    private String timeSlot;
    private String courierName;
    private DeliveryStatus status;
    private String customerName;
    private String deliveryAddress;
    private String contactNumber;
}
