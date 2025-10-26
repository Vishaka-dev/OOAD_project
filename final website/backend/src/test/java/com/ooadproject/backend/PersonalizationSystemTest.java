package com.ooadproject.backend;

import com.ooadproject.backend.dto.PersonalizationDTO;
import com.ooadproject.backend.utils.PersonalizationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PersonalizationSystemTest {

    @Test
    public void testPersonalizationDTOCreation() {
        // Create a complete personalization DTO
        PersonalizationDTO.TeddyDetails teddy = new PersonalizationDTO.TeddyDetails(true, "Small Bear", "Blue");
        PersonalizationDTO.FlowerDetails flowers = new PersonalizationDTO.FlowerDetails(12, "Red");

        PersonalizationDTO personalization = new PersonalizationDTO();
        personalization.setCustomizationId("CUST-12345");
        personalization.setOccasion("Birthday");
        personalization.setTeddy(teddy);
        personalization.setFlowers(flowers);
        personalization.setWrappingPaper("Gold Foil");
        personalization.setSoftToys("Bunny");
        personalization.setFeltDesign("Happy Birthday Sarah");
        personalization.setCustomMessage("With love from Mom");

        // Test conversion to Map
        Map<String, Object> map = personalization.toMap();

        assertNotNull(map);
        assertEquals("CUST-12345", map.get("customization_id"));
        assertEquals("Birthday", map.get("occasion"));
        assertEquals("Gold Foil", map.get("wrapping_paper"));
        assertEquals("Bunny", map.get("soft_toys"));
        assertEquals("Happy Birthday Sarah", map.get("felt_design"));
        assertEquals("With love from Mom", map.get("custom_message"));

        // Test nested objects
        @SuppressWarnings("unchecked")
        Map<String, Object> teddyMap = (Map<String, Object>) map.get("teddy");
        assertNotNull(teddyMap);
        assertEquals(true, teddyMap.get("included"));
        assertEquals("Small Bear", teddyMap.get("type"));
        assertEquals("Blue", teddyMap.get("color"));

        @SuppressWarnings("unchecked")
        Map<String, Object> flowersMap = (Map<String, Object>) map.get("flowers");
        assertNotNull(flowersMap);
        assertEquals(12, flowersMap.get("count"));
        assertEquals("Red", flowersMap.get("color"));
    }

    @Test
    public void testPersonalizationDTOFromMap() {
        // Create a map representing personalization data
        Map<String, Object> map = Map.of(
                "customization_id", "CUST-67890",
                "occasion", "Graduation",
                "teddy", Map.of(
                        "included", true,
                        "type", "fluffy",
                        "color", "Brown"),
                "flowers", Map.of(
                        "count", 5,
                        "color", "White"),
                "wrapping_paper", "Premium",
                "soft_toys", "Yes",
                "felt_design", "Congratulations Graduate",
                "custom_message", "Proud of you!",
                "extra_cost", 250.00);

        // Convert from Map to DTO
        PersonalizationDTO dto = PersonalizationDTO.fromMap(map);

        assertNotNull(dto);
        assertEquals("CUST-67890", dto.getCustomizationId());
        assertEquals("Graduation", dto.getOccasion());
        assertEquals("Premium", dto.getWrappingPaper());
        assertEquals("Yes", dto.getSoftToys());
        assertEquals("Congratulations Graduate", dto.getFeltDesign());
        assertEquals("Proud of you!", dto.getCustomMessage());
        assertEquals(BigDecimal.valueOf(250.00), dto.getExtraCost());

        // Test nested objects
        assertNotNull(dto.getTeddy());
        assertEquals(true, dto.getTeddy().getIncluded());
        assertEquals("fluffy", dto.getTeddy().getType());
        assertEquals("Brown", dto.getTeddy().getColor());

        assertNotNull(dto.getFlowers());
        assertEquals(5, dto.getFlowers().getCount());
        assertEquals("White", dto.getFlowers().getColor());
    }

    @Test
    public void testExtraCostCalculation() {
        PersonalizationDTO personalization = new PersonalizationDTO();

        // Test occasion pricing
        personalization.setOccasion("Birthday");
        assertEquals(BigDecimal.valueOf(3), personalization.calculateExtraCost());

        personalization.setOccasion("Graduation");
        assertEquals(BigDecimal.valueOf(5), personalization.calculateExtraCost());

        personalization.setOccasion("Valentine");
        assertEquals(BigDecimal.valueOf(8), personalization.calculateExtraCost());

        personalization.setOccasion("Mini");
        assertEquals(BigDecimal.valueOf(2), personalization.calculateExtraCost());

        // Test teddy pricing
        PersonalizationDTO.TeddyDetails teddy = new PersonalizationDTO.TeddyDetails(true, "Small Bear", "Blue");
        personalization.setTeddy(teddy);
        assertEquals(BigDecimal.valueOf(17), personalization.calculateExtraCost()); // 2 (Mini) + 15 (teddy)

        teddy.setType("handmade");
        assertEquals(BigDecimal.valueOf(22), personalization.calculateExtraCost()); // 2 (Mini) + 15 (teddy) + 5
                                                                                    // (handmade)

        teddy.setType("fluffy");
        assertEquals(BigDecimal.valueOf(27), personalization.calculateExtraCost()); // 2 (Mini) + 15 (teddy) + 10
                                                                                    // (fluffy)

        // Test flowers pricing
        PersonalizationDTO.FlowerDetails flowers = new PersonalizationDTO.FlowerDetails(12, "Red");
        personalization.setFlowers(flowers);
        assertEquals(BigDecimal.valueOf(39), personalization.calculateExtraCost()); // 2 + 15 + 10 + 12

        // Test wrapping paper pricing
        personalization.setWrappingPaper("Premium");
        assertEquals(BigDecimal.valueOf(42), personalization.calculateExtraCost()); // 39 + 3

        personalization.setWrappingPaper("Gift Box");
        assertEquals(BigDecimal.valueOf(44), personalization.calculateExtraCost()); // 39 + 5

        // Test soft toys pricing
        personalization.setSoftToys("Yes");
        assertEquals(BigDecimal.valueOf(52), personalization.calculateExtraCost()); // 44 + 8

        // Test felt design pricing
        personalization.setFeltDesign("Custom Text");
        assertEquals(BigDecimal.valueOf(57), personalization.calculateExtraCost()); // 52 + 5
    }

    @Test
    public void testPersonalizationUtils() {
        Map<String, Object> personalizationDetails = Map.of(
                "customization_id", "CUST-TEST-001",
                "occasion", "Birthday",
                "teddy", Map.of(
                        "included", true,
                        "type", "Small Bear",
                        "color", "Blue"),
                "flowers", Map.of(
                        "count", 12,
                        "color", "Red"),
                "wrapping_paper", "Gold Foil",
                "soft_toys", "Yes",
                "felt_design", "Happy Birthday",
                "custom_message", "With love");

        // Test summary generation
        String summary = PersonalizationUtils.getPersonalizationSummary(personalizationDetails);
        assertTrue(summary.contains("Occasion: Birthday"));
        assertTrue(summary.contains("Teddy: Small Bear (Blue)"));
        assertTrue(summary.contains("Flowers: 12 (Red)"));
        assertTrue(summary.contains("Wrapping: Gold Foil"));
        assertTrue(summary.contains("Soft Toys: Yes"));
        assertTrue(summary.contains("Custom Design: Happy Birthday"));
        assertTrue(summary.contains("Message: With love"));

        // Test individual field extraction
        assertEquals("CUST-TEST-001", PersonalizationUtils.getCustomizationId(personalizationDetails));
        assertEquals("Birthday", PersonalizationUtils.getOccasion(personalizationDetails));

        PersonalizationDTO.TeddyDetails teddy = PersonalizationUtils.getTeddyDetails(personalizationDetails);
        assertNotNull(teddy);
        assertEquals(true, teddy.getIncluded());
        assertEquals("Small Bear", teddy.getType());
        assertEquals("Blue", teddy.getColor());

        PersonalizationDTO.FlowerDetails flowers = PersonalizationUtils.getFlowerDetails(personalizationDetails);
        assertNotNull(flowers);
        assertEquals(12, flowers.getCount());
        assertEquals("Red", flowers.getColor());

        // Test hasPersonalization
        assertTrue(PersonalizationUtils.hasPersonalization(personalizationDetails));

        // Test empty personalization
        Map<String, Object> emptyPersonalization = Map.of();
        assertFalse(PersonalizationUtils.hasPersonalization(emptyPersonalization));
        assertEquals("No personalization", PersonalizationUtils.getPersonalizationSummary(emptyPersonalization));
    }

    @Test
    public void testCustomizationIdGeneration() throws InterruptedException {
        String id1 = PersonalizationDTO.generateCustomizationId();
        Thread.sleep(1); // Ensure different timestamps
        String id2 = PersonalizationDTO.generateCustomizationId();

        assertNotNull(id1);
        assertNotNull(id2);
        assertTrue(id1.startsWith("CUST-"));
        assertTrue(id2.startsWith("CUST-"));
        assertNotEquals(id1, id2); // Should be unique
    }
}
