import { PersonalizationDetails } from '@/types/product';

/**
 * Helper functions for working with personalization data in the new JSON format
 */

/**
 * Convert legacy flat personalization format to new JSON structure
 */
export function convertToNewFormat(legacyDetails: PersonalizationDetails): PersonalizationDetails {
  const newFormat: PersonalizationDetails = {
    customization_id: `CUST-${Date.now()}`,
  };

  // Copy direct fields
  if (legacyDetails.occasion) newFormat.occasion = legacyDetails.occasion;
  if (legacyDetails.wrapping_paper) newFormat.wrapping_paper = legacyDetails.wrapping_paper;
  if (legacyDetails.felt_design) newFormat.felt_design = legacyDetails.felt_design;
  if (legacyDetails.custom_message) newFormat.custom_message = legacyDetails.custom_message;

  // Handle legacy wrapping paper field
  if (legacyDetails.wrappingPaper && !newFormat.wrapping_paper) {
    newFormat.wrapping_paper = legacyDetails.wrappingPaper;
  }

  // Handle legacy soft toys field
  if (legacyDetails.softToys && !newFormat.soft_toys) {
    newFormat.soft_toys = legacyDetails.softToys;
  }

  // Handle teddy bear data
  if (legacyDetails.teddy || legacyDetails.teddyType || legacyDetails.teddyColor) {
    newFormat.teddy = {
      included: legacyDetails.teddy === "With" || legacyDetails.teddy === "Yes",
      type: legacyDetails.teddyType,
      color: legacyDetails.teddyColor,
    };
  }

  // Handle flower data
  if (legacyDetails.flowersCount || legacyDetails.flowersColor) {
    const count = legacyDetails.flowersCount ? parseInt(legacyDetails.flowersCount) : undefined;
    newFormat.flowers = {
      count,
      color: legacyDetails.flowersColor,
    };
  }

  // Calculate and set extra cost
  newFormat.extra_cost = calculateExtraCost(newFormat);

  return newFormat;
}

/**
 * Convert new JSON format back to legacy flat format for backward compatibility
 */
export function convertToLegacyFormat(newDetails: PersonalizationDetails): PersonalizationDetails {
  const legacyFormat: PersonalizationDetails = {};

  // Copy direct fields
  if (newDetails.occasion) legacyFormat.occasion = newDetails.occasion;
  if (newDetails.wrapping_paper) legacyFormat.wrappingPaper = newDetails.wrapping_paper;
  if (newDetails.felt_design) legacyFormat.feltDesign = newDetails.felt_design;
  if (newDetails.custom_message) legacyFormat.massage = newDetails.custom_message;

  // Handle teddy bear data
  if (newDetails.teddy) {
    legacyFormat.teddy = newDetails.teddy.included ? "With" : "Without";
    legacyFormat.teddyType = newDetails.teddy.type;
    legacyFormat.teddyColor = newDetails.teddy.color;
  }

  // Handle flower data
  if (newDetails.flowers) {
    legacyFormat.flowersCount = newDetails.flowers.count?.toString();
    legacyFormat.flowersColor = newDetails.flowers.color;
  }

  // Handle soft toys
  if (newDetails.soft_toys) {
    legacyFormat.softToys = newDetails.soft_toys;
  }

  return legacyFormat;
}

/**
 * Calculate extra cost based on personalization options (matches backend logic)
 */
export function calculateExtraCost(details: PersonalizationDetails): number {
  let extraCost = 0;

  // Occasion pricing
  if (details.occasion) {
    switch (details.occasion) {
      case "Graduation":
        extraCost += 5;
        break;
      case "Birthday":
        extraCost += 3;
        break;
      case "Valentine":
        extraCost += 8;
        break;
      case "Mini":
        extraCost += 2;
        break;
    }
  }

  // Teddy pricing
  if (details.teddy && details.teddy.included) {
    extraCost += 15;
    
    if (details.teddy.type) {
      switch (details.teddy.type) {
        case "handmade":
          extraCost += 5;
          break;
        case "fluffy":
          extraCost += 10;
          break;
      }
    }
  }

  // Flowers pricing
  if (details.flowers && details.flowers.count) {
    extraCost += details.flowers.count;
  }

  // Wrapping paper pricing
  if (details.wrapping_paper) {
    switch (details.wrapping_paper) {
      case "Premium":
        extraCost += 3;
        break;
      case "Gift Box":
        extraCost += 5;
        break;
    }
  }

  // Soft toys pricing
  if (details.soft_toys === "Yes") {
    extraCost += 8;
  }

  // Custom felt design pricing
  if (details.felt_design && details.felt_design.trim()) {
    extraCost += 5;
  }

  return extraCost;
}

/**
 * Generate a formatted summary of personalization details for display
 */
export function getPersonalizationSummary(details: PersonalizationDetails): string {
  if (!details || Object.keys(details).length === 0) {
    return "No personalization";
  }

  const parts: string[] = [];

  // Add occasion
  if (details.occasion) {
    parts.push(`Occasion: ${details.occasion}`);
  }

  // Add teddy details
  if (details.teddy && details.teddy.included) {
    let teddyText = "Teddy: ";
    if (details.teddy.type) teddyText += details.teddy.type;
    if (details.teddy.color) teddyText += ` (${details.teddy.color})`;
    parts.push(teddyText);
  }

  // Add flower details
  if (details.flowers && details.flowers.count) {
    let flowerText = `Flowers: ${details.flowers.count}`;
    if (details.flowers.color) flowerText += ` (${details.flowers.color})`;
    parts.push(flowerText);
  }

  // Add wrapping paper
  if (details.wrapping_paper) {
    parts.push(`Wrapping: ${details.wrapping_paper}`);
  }

  // Add soft toys
  if (details.soft_toys === "Yes") {
    parts.push("Soft Toys: Yes");
  }

  // Add felt design
  if (details.felt_design && details.felt_design.trim()) {
    parts.push(`Custom Design: ${details.felt_design}`);
  }

  // Add custom message
  if (details.custom_message && details.custom_message.trim()) {
    parts.push(`Message: ${details.custom_message}`);
  }

  return parts.length > 0 ? parts.join(", ") : "No personalization";
}

/**
 * Check if personalization details have any meaningful content
 */
export function hasPersonalization(details: PersonalizationDetails): boolean {
  if (!details || Object.keys(details).length === 0) {
    return false;
  }

  return !!(
    details.occasion ||
    (details.teddy && details.teddy.included) ||
    (details.flowers && details.flowers.count) ||
    details.wrapping_paper ||
    details.soft_toys ||
    details.felt_design ||
    details.custom_message
  );
}

/**
 * Get customization ID from personalization details
 */
export function getCustomizationId(details: PersonalizationDetails): string | undefined {
  return details.customization_id;
}

/**
 * Generate a new customization ID
 */
export function generateCustomizationId(): string {
  return `CUST-${Date.now()}`;
}
