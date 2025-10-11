# Personalization API Usage Guide

This document explains how to use the new JSON-based personalization system for the Gift Shop POS.

## Overview

The personalization system has been converted from individual database columns to a flexible JSON approach. This provides better flexibility and easier extensibility.

## JSON Structure

The `personalization_details` JSON column stores all personalization data in this format:

```json
{
  "customization_id": "CUST-1704892860123",
  "occasion": "Birthday",
  "teddy": {
    "included": true,
    "type": "Small Bear",
    "color": "Blue"
  },
  "flowers": {
    "count": 12,
    "color": "Red"
  },
  "wrapping_paper": "Gold Foil",
  "soft_toys": "Bunny",
  "felt_design": "Happy Birthday Sarah",
  "custom_message": "With love from Mom",
  "extra_cost": 250.0
}
```

## API Endpoints

### 1. Add Item to Cart with Personalization

**Endpoint:** `POST /api/cart/add-with-personalization`

**Request Body:**

```json
{
  "productId": 101,
  "quantity": 1,
  "personalization": {
    "customization_id": "CUST-1704892860123",
    "occasion": "Birthday",
    "teddy": {
      "included": true,
      "type": "Small Bear",
      "color": "Blue"
    },
    "flowers": {
      "count": 12,
      "color": "Red"
    },
    "wrapping_paper": "Gold Foil",
    "soft_toys": "Bunny",
    "felt_design": "Happy Birthday Sarah",
    "custom_message": "With love from Mom"
  }
}
```

**Response:**

```json
{
  "message": "Item added to cart successfully"
}
```

### 2. Add Item to Cart (Legacy Support)

**Endpoint:** `POST /api/cart/add`

**Parameters:**

- `productId` (query param): Product ID
- `quantity` (query param): Quantity
- Request body: `PersonalizationDTO` object

### 3. Get Cart Items

**Endpoint:** `GET /api/cart`

**Response:**

```json
[
  {
    "itemId": 1,
    "productId": 101,
    "productName": "Custom Gift Box",
    "productPrice": 25.0,
    "imageUrl": "/images/gift-box.jpg",
    "quantity": 1,
    "personalizationDetails": {
      "customization_id": "CUST-1704892860123",
      "occasion": "Birthday",
      "teddy": {
        "included": true,
        "type": "Small Bear",
        "color": "Blue"
      },
      "flowers": {
        "count": 12,
        "color": "Red"
      },
      "wrapping_paper": "Gold Foil",
      "soft_toys": "Bunny",
      "felt_design": "Happy Birthday Sarah",
      "custom_message": "With love from Mom",
      "extra_cost": 250.0
    },
    "extraPrice": 250.0,
    "itemTotal": 275.0
  }
]
```

## Personalization Options

### Occasion Types

- `"Birthday"` (+$3)
- `"Graduation"` (+$5)
- `"Valentine"` (+$8)
- `"Mini"` (+$2)

### Teddy Options

```json
{
  "included": true, // Base teddy cost: +$15
  "type": "Small Bear", // "Small Bear", "handmade" (+$5), "fluffy" (+$10)
  "color": "Blue" // Any color
}
```

### Flower Options

```json
{
  "count": 12, // Any number (cost = count * $1)
  "color": "Red" // Any color
}
```

### Wrapping Paper Options

- `"Premium"` (+$3)
- `"Gift Box"` (+$5)

### Soft Toys Options

- `"Yes"` (+$8)
- `"No"` (no extra cost)

### Custom Design

- `felt_design`: Any custom text (+$5)

## Frontend Integration Example

### JavaScript/TypeScript

```javascript
// Add to cart with personalization
const addToCartWithPersonalization = async (
  productId,
  quantity,
  personalizationData
) => {
  const requestData = {
    productId: productId,
    quantity: quantity,
    personalization: {
      customization_id: `CUST-${Date.now()}`,
      occasion: personalizationData.occasion,
      teddy: {
        included: personalizationData.includeTeddy,
        type: personalizationData.teddyType,
        color: personalizationData.teddyColor,
      },
      flowers: {
        count: personalizationData.flowerCount,
        color: personalizationData.flowerColor,
      },
      wrapping_paper: personalizationData.wrappingPaper,
      soft_toys: personalizationData.softToys,
      felt_design: personalizationData.customDesign,
      custom_message: personalizationData.customMessage,
    },
  };

  try {
    const response = await fetch("/api/cart/add-with-personalization", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(requestData),
    });

    const result = await response.json();
    console.log("Item added to cart:", result);
  } catch (error) {
    console.error("Error adding to cart:", error);
  }
};

// Get cart items and display personalization
const getCartItems = async () => {
  try {
    const response = await fetch("/api/cart", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const cartItems = await response.json();

    cartItems.forEach((item) => {
      console.log(`Product: ${item.productName}`);
      console.log(`Personalization: ${item.personalizationSummary}`);
      console.log(`Extra Cost: $${item.extraPrice}`);
      console.log(`Total: $${item.itemTotal}`);
    });
  } catch (error) {
    console.error("Error getting cart items:", error);
  }
};
```

### React Component Example

```jsx
import React, { useState } from "react";

const PersonalizationForm = ({ productId, onAddToCart }) => {
  const [personalization, setPersonalization] = useState({
    occasion: "",
    includeTeddy: false,
    teddyType: "",
    teddyColor: "",
    flowerCount: 0,
    flowerColor: "",
    wrappingPaper: "",
    softToys: "No",
    customDesign: "",
    customMessage: "",
  });

  const handleSubmit = (e) => {
    e.preventDefault();

    const personalizationData = {
      customization_id: `CUST-${Date.now()}`,
      occasion: personalization.occasion,
      teddy: personalization.includeTeddy
        ? {
            included: true,
            type: personalization.teddyType,
            color: personalization.teddyColor,
          }
        : null,
      flowers:
        personalization.flowerCount > 0
          ? {
              count: personalization.flowerCount,
              color: personalization.flowerColor,
            }
          : null,
      wrapping_paper: personalization.wrappingPaper,
      soft_toys: personalization.softToys,
      felt_design: personalization.customDesign,
      custom_message: personalization.customMessage,
    };

    onAddToCart(productId, 1, personalizationData);
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Occasion Selection */}
      <div>
        <label>Occasion:</label>
        <select
          value={personalization.occasion}
          onChange={(e) =>
            setPersonalization({ ...personalization, occasion: e.target.value })
          }
        >
          <option value="">Select Occasion</option>
          <option value="Birthday">Birthday (+$3)</option>
          <option value="Graduation">Graduation (+$5)</option>
          <option value="Valentine">Valentine (+$8)</option>
          <option value="Mini">Mini (+$2)</option>
        </select>
      </div>

      {/* Teddy Bear Options */}
      <div>
        <label>
          <input
            type="checkbox"
            checked={personalization.includeTeddy}
            onChange={(e) =>
              setPersonalization({
                ...personalization,
                includeTeddy: e.target.checked,
              })
            }
          />
          Include Teddy Bear (+$15)
        </label>
      </div>

      {personalization.includeTeddy && (
        <div>
          <label>Teddy Type:</label>
          <select
            value={personalization.teddyType}
            onChange={(e) =>
              setPersonalization({
                ...personalization,
                teddyType: e.target.value,
              })
            }
          >
            <option value="Small Bear">Small Bear</option>
            <option value="handmade">Handmade (+$5)</option>
            <option value="fluffy">Fluffy (+$10)</option>
          </select>
        </div>
      )}

      {/* Flowers */}
      <div>
        <label>Flower Count:</label>
        <input
          type="number"
          value={personalization.flowerCount}
          onChange={(e) =>
            setPersonalization({
              ...personalization,
              flowerCount: parseInt(e.target.value) || 0,
            })
          }
          min="0"
        />
        <span>
          (+${personalization.flowerCount} for {personalization.flowerCount}{" "}
          flowers)
        </span>
      </div>

      {/* Custom Design */}
      <div>
        <label>Custom Design Text:</label>
        <input
          type="text"
          value={personalization.customDesign}
          onChange={(e) =>
            setPersonalization({
              ...personalization,
              customDesign: e.target.value,
            })
          }
          placeholder="Enter custom text for felt design"
          maxLength="255"
        />
        <span>(+$5)</span>
      </div>

      <button type="submit">Add to Cart</button>
    </form>
  );
};

export default PersonalizationForm;
```

## Helper Methods

The DTOs include helper methods for working with personalization data:

```java
// Get formatted summary
String summary = cartItemDTO.getPersonalizationSummary();
// Output: "Occasion: Birthday, Teddy: Small Bear (Blue), Flowers: 12 (Red), Wrapping: Gold Foil"

// Check if personalized
boolean hasPersonalization = cartItemDTO.hasPersonalization();

// Get specific details
String customizationId = cartItemDTO.getCustomizationId();
String occasion = cartItemDTO.getOccasion();
PersonalizationDTO.TeddyDetails teddy = cartItemDTO.getTeddyDetails();
PersonalizationDTO.FlowerDetails flowers = cartItemDTO.getFlowerDetails();

// Get order display string
String orderDisplay = cartItemDTO.getOrderDisplayString();
```

## Migration Notes

- The old individual columns have been removed from `cart_items` and `order_items` tables
- All personalization data is now stored in the `personalization_details` JSON column
- The system automatically calculates extra costs based on personalization options
- Customization IDs are auto-generated if not provided
- Backward compatibility is maintained through the PersonalizationDTO conversion methods

## Benefits

1. **Flexibility**: Easy to add new personalization fields without schema changes
2. **Clean Schema**: No need for multiple nullable columns
3. **Better Performance**: Single JSON column vs multiple columns
4. **Type Safety**: Structured DTO on backend with validation
5. **Frontend Friendly**: JSON maps directly to JavaScript objects
6. **Future-Proof**: Easy to extend personalization options
