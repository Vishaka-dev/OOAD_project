package com.ooadproject.backend.dto;

import com.ooadproject.backend.utils.PersonalizationUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CartItemDTO {
    private Integer itemId;
    private Integer productId;
    private String productName;
    private BigDecimal productPrice;
    private String imageUrl;
    private Integer quantity;
    private Map<String, Object> personalizationDetails;
    private BigDecimal extraPrice = BigDecimal.ZERO;
    private BigDecimal itemTotal;

    // Helper methods for working with personalization details

    /**
     * Get a formatted summary of personalization details
     */
    public String getPersonalizationSummary() {
        return PersonalizationUtils.getPersonalizationSummary(personalizationDetails);
    }

    /**
     * Get the customization ID from personalization details
     */
    public String getCustomizationId() {
        return PersonalizationUtils.getCustomizationId(personalizationDetails);
    }

    /**
     * Get the occasion from personalization details
     */
    public String getOccasion() {
        return PersonalizationUtils.getOccasion(personalizationDetails);
    }

    /**
     * Get teddy details from personalization details
     */
    public PersonalizationDTO.TeddyDetails getTeddyDetails() {
        return PersonalizationUtils.getTeddyDetails(personalizationDetails);
    }

    /**
     * Get flower details from personalization details
     */
    public PersonalizationDTO.FlowerDetails getFlowerDetails() {
        return PersonalizationUtils.getFlowerDetails(personalizationDetails);
    }

    /**
     * Check if this cart item has personalization
     */
    public boolean hasPersonalization() {
        return PersonalizationUtils.hasPersonalization(personalizationDetails);
    }

    /**
     * Get a formatted display string for order purposes
     */
    public String getOrderDisplayString() {
        return PersonalizationUtils.getOrderPersonalizationDisplay(personalizationDetails);
    }
}