package com.ooadproject.backend.utils;

import com.ooadproject.backend.dto.PersonalizationDTO;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Utility class for working with personalization data in JSON format
 */
public class PersonalizationUtils {

    /**
     * Extract a formatted personalization summary for display purposes
     */
    public static String getPersonalizationSummary(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null || personalizationDetails.isEmpty()) {
            return "No personalization";
        }

        StringBuilder summary = new StringBuilder();

        // Add occasion
        String occasion = (String) personalizationDetails.get("occasion");
        if (occasion != null) {
            summary.append("Occasion: ").append(occasion);
        }

        // Add teddy details
        Object teddyObj = personalizationDetails.get("teddy");
        if (teddyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> teddyMap = (Map<String, Object>) teddyObj;

            Boolean included = (Boolean) teddyMap.get("included");
            if (Boolean.TRUE.equals(included)) {
                if (summary.length() > 0)
                    summary.append(", ");
                summary.append("Teddy: ");

                String type = (String) teddyMap.get("type");
                String color = (String) teddyMap.get("color");

                if (type != null)
                    summary.append(type);
                if (color != null)
                    summary.append(" (").append(color).append(")");
            }
        }

        // Add flower details
        Object flowersObj = personalizationDetails.get("flowers");
        if (flowersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> flowersMap = (Map<String, Object>) flowersObj;

            Object countObj = flowersMap.get("count");
            String color = (String) flowersMap.get("color");

            if (countObj instanceof Number) {
                if (summary.length() > 0)
                    summary.append(", ");
                summary.append("Flowers: ").append(((Number) countObj).intValue());
                if (color != null) {
                    summary.append(" (").append(color).append(")");
                }
            }
        }

        // Add wrapping paper
        String wrappingPaper = (String) personalizationDetails.get("wrapping_paper");
        if (wrappingPaper != null) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("Wrapping: ").append(wrappingPaper);
        }

        // Add soft toys
        String softToys = (String) personalizationDetails.get("soft_toys");
        if ("Yes".equals(softToys)) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("Soft Toys: Yes");
        }

        // Add felt design
        String feltDesign = (String) personalizationDetails.get("felt_design");
        if (feltDesign != null && !feltDesign.trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("Custom Design: ").append(feltDesign);
        }

        // Add custom message
        String customMessage = (String) personalizationDetails.get("custom_message");
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("Message: ").append(customMessage);
        }

        return summary.length() > 0 ? summary.toString() : "No personalization";
    }

    /**
     * Extract the customization ID from personalization details
     */
    public static String getCustomizationId(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null) {
            return null;
        }
        return (String) personalizationDetails.get("customization_id");
    }

    /**
     * Extract the occasion from personalization details
     */
    public static String getOccasion(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null) {
            return null;
        }
        return (String) personalizationDetails.get("occasion");
    }

    /**
     * Extract teddy details from personalization details
     */
    public static PersonalizationDTO.TeddyDetails getTeddyDetails(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null) {
            return null;
        }

        Object teddyObj = personalizationDetails.get("teddy");
        if (teddyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> teddyMap = (Map<String, Object>) teddyObj;

            PersonalizationDTO.TeddyDetails teddyDetails = new PersonalizationDTO.TeddyDetails();
            teddyDetails.setIncluded((Boolean) teddyMap.get("included"));
            teddyDetails.setType((String) teddyMap.get("type"));
            teddyDetails.setColor((String) teddyMap.get("color"));
            return teddyDetails;
        }

        return null;
    }

    /**
     * Extract flower details from personalization details
     */
    public static PersonalizationDTO.FlowerDetails getFlowerDetails(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null) {
            return null;
        }

        Object flowersObj = personalizationDetails.get("flowers");
        if (flowersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> flowersMap = (Map<String, Object>) flowersObj;

            PersonalizationDTO.FlowerDetails flowerDetails = new PersonalizationDTO.FlowerDetails();
            flowerDetails.setColor((String) flowersMap.get("color"));

            Object countObj = flowersMap.get("count");
            if (countObj instanceof Number) {
                flowerDetails.setCount(((Number) countObj).intValue());
            }
            return flowerDetails;
        }

        return null;
    }

    /**
     * Extract extra cost from personalization details
     */
    public static BigDecimal getExtraCost(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null) {
            return BigDecimal.ZERO;
        }

        Object extraCostObj = personalizationDetails.get("extra_cost");
        if (extraCostObj instanceof BigDecimal) {
            return (BigDecimal) extraCostObj;
        } else if (extraCostObj instanceof Number) {
            return BigDecimal.valueOf(((Number) extraCostObj).doubleValue());
        }

        return BigDecimal.ZERO;
    }

    /**
     * Check if personalization details have any meaningful content
     */
    public static boolean hasPersonalization(Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null || personalizationDetails.isEmpty()) {
            return false;
        }

        // Check if any meaningful fields are present
        return personalizationDetails.containsKey("occasion") ||
                personalizationDetails.containsKey("teddy") ||
                personalizationDetails.containsKey("flowers") ||
                personalizationDetails.containsKey("wrapping_paper") ||
                personalizationDetails.containsKey("soft_toys") ||
                personalizationDetails.containsKey("felt_design") ||
                personalizationDetails.containsKey("custom_message");
    }

    /**
     * Convert personalization details to a formatted string for order display
     */
    public static String getOrderPersonalizationDisplay(Map<String, Object> personalizationDetails) {
        if (!hasPersonalization(personalizationDetails)) {
            return "Standard product";
        }

        StringBuilder display = new StringBuilder();
        display.append("Personalized:\n");

        String summary = getPersonalizationSummary(personalizationDetails);
        String[] parts = summary.split(", ");

        for (String part : parts) {
            display.append("• ").append(part).append("\n");
        }

        // Add customization ID if present
        String customizationId = getCustomizationId(personalizationDetails);
        if (customizationId != null) {
            display.append("• ID: ").append(customizationId);
        }

        return display.toString().trim();
    }
}
