package com.ooadproject.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizationDTO {

    @JsonProperty("customization_id")
    private String customizationId;

    private String occasion;

    private TeddyDetails teddy;

    private FlowerDetails flowers;

    @JsonProperty("wrapping_paper")
    private String wrappingPaper;

    @JsonProperty("soft_toys")
    private String softToys;

    @JsonProperty("felt_design")
    private String feltDesign;

    @JsonProperty("custom_message")
    private String customMessage;

    @JsonProperty("extra_cost")
    private BigDecimal extraCost;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeddyDetails {
        private Boolean included;
        private String type;
        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlowerDetails {
        private Integer count;
        private String color;
    }

    /**
     * Convert PersonalizationDTO to Map for JSON storage in database
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        if (customizationId != null) {
            map.put("customization_id", customizationId);
        }
        if (occasion != null) {
            map.put("occasion", occasion);
        }

        if (teddy != null) {
            Map<String, Object> teddyMap = new HashMap<>();
            teddyMap.put("included", teddy.getIncluded());
            teddyMap.put("type", teddy.getType());
            teddyMap.put("color", teddy.getColor());
            map.put("teddy", teddyMap);
        }

        if (flowers != null) {
            Map<String, Object> flowersMap = new HashMap<>();
            flowersMap.put("count", flowers.getCount());
            flowersMap.put("color", flowers.getColor());
            map.put("flowers", flowersMap);
        }

        if (wrappingPaper != null) {
            map.put("wrapping_paper", wrappingPaper);
        }
        if (softToys != null) {
            map.put("soft_toys", softToys);
        }
        if (feltDesign != null) {
            map.put("felt_design", feltDesign);
        }
        if (customMessage != null) {
            map.put("custom_message", customMessage);
        }
        if (extraCost != null) {
            map.put("extra_cost", extraCost);
        }

        return map;
    }

    /**
     * Create PersonalizationDTO from Map (when reading from database)
     */
    public static PersonalizationDTO fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        PersonalizationDTO dto = new PersonalizationDTO();

        dto.setCustomizationId((String) map.get("customization_id"));
        dto.setOccasion((String) map.get("occasion"));
        dto.setWrappingPaper((String) map.get("wrapping_paper"));
        dto.setSoftToys((String) map.get("soft_toys"));
        dto.setFeltDesign((String) map.get("felt_design"));
        dto.setCustomMessage((String) map.get("custom_message"));

        Object extraCostObj = map.get("extra_cost");
        if (extraCostObj != null) {
            if (extraCostObj instanceof BigDecimal) {
                dto.setExtraCost((BigDecimal) extraCostObj);
            } else if (extraCostObj instanceof Number) {
                dto.setExtraCost(BigDecimal.valueOf(((Number) extraCostObj).doubleValue()));
            }
        }

        // Handle teddy details
        Object teddyObj = map.get("teddy");
        if (teddyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> teddyMap = (Map<String, Object>) teddyObj;

            TeddyDetails teddyDetails = new TeddyDetails();
            teddyDetails.setIncluded((Boolean) teddyMap.get("included"));
            teddyDetails.setType((String) teddyMap.get("type"));
            teddyDetails.setColor((String) teddyMap.get("color"));
            dto.setTeddy(teddyDetails);
        }

        // Handle flower details
        Object flowersObj = map.get("flowers");
        if (flowersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> flowersMap = (Map<String, Object>) flowersObj;

            FlowerDetails flowerDetails = new FlowerDetails();
            flowerDetails.setColor((String) flowersMap.get("color"));

            Object countObj = flowersMap.get("count");
            if (countObj instanceof Number) {
                flowerDetails.setCount(((Number) countObj).intValue());
            }
            dto.setFlowers(flowerDetails);
        }

        return dto;
    }

    /**
     * Generate a unique customization ID
     */
    public static String generateCustomizationId() {
        return "CUST-" + System.currentTimeMillis();
    }

    /**
     * Calculate extra cost based on personalization details
     */
    public BigDecimal calculateExtraCost() {
        BigDecimal cost = BigDecimal.ZERO;

        // Occasion pricing
        if (occasion != null) {
            switch (occasion) {
                case "Graduation" -> cost = cost.add(BigDecimal.valueOf(5));
                case "Birthday" -> cost = cost.add(BigDecimal.valueOf(3));
                case "Valentine" -> cost = cost.add(BigDecimal.valueOf(8));
                case "Mini" -> cost = cost.add(BigDecimal.valueOf(2));
            }
        }

        // Teddy pricing
        if (teddy != null && Boolean.TRUE.equals(teddy.getIncluded())) {
            cost = cost.add(BigDecimal.valueOf(15));

            if (teddy.getType() != null) {
                switch (teddy.getType()) {
                    case "handmade" -> cost = cost.add(BigDecimal.valueOf(5));
                    case "fluffy" -> cost = cost.add(BigDecimal.valueOf(10));
                }
            }
        }

        // Flowers pricing
        if (flowers != null && flowers.getCount() != null) {
            cost = cost.add(BigDecimal.valueOf(flowers.getCount()));
        }

        // Wrapping paper pricing
        if (wrappingPaper != null) {
            switch (wrappingPaper) {
                case "Premium" -> cost = cost.add(BigDecimal.valueOf(3));
                case "Gift Box" -> cost = cost.add(BigDecimal.valueOf(5));
            }
        }

        // Soft toys pricing
        if ("Yes".equals(softToys)) {
            cost = cost.add(BigDecimal.valueOf(8));
        }

        // Custom felt design pricing
        if (feltDesign != null && !feltDesign.trim().isEmpty()) {
            cost = cost.add(BigDecimal.valueOf(5));
        }

        return cost;
    }
}
