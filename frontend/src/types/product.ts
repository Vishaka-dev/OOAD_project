// Backend API Types (Member A - Product Catalog)
export interface Category {
  categoryId: number;
  name: string;
  description: string;
  productCount: number;
}

export interface Product {
  productId: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  stockQuantity: number;
  inStock: boolean;
}

// Frontend UI Types (for compatibility with existing components)
export interface UIProduct {
  id: string;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  image: string;
  category: string;
  size: 'Small' | 'Medium' | 'Large' | 'Extra Large';
  stock: number;
  featured?: boolean;
  rating: number;
  reviews: number;
}

export interface CartItem extends UIProduct {
  quantity: number;
  backendId?: number; // For backend cart item ID
}

export interface Order {
  id: string;
  items: CartItem[];
  total: number;
  status: 'pending' | 'processing' | 'shipped' | 'delivered';
  createdAt: Date;
  customerName?: string;
  customerEmail?: string;
}

// API Request/Response Types
export interface ProductFilterParams {
  name?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  page?: number;
  size?: number;
}

export interface ProductSearchParams {
  name: string;
  page?: number;
  size?: number;
}

export interface PriceRangeParams {
  minPrice: number;
  maxPrice: number;
  page?: number;
  size?: number;
}

export interface CreateCategoryRequest {
  name: string;
  description: string;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
}

export interface CreateProductRequest {
  categoryId: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  stockQuantity: number;
}

export interface UpdateProductRequest {
  categoryId?: number;
  name?: string;
  description?: string;
  price?: number;
  imageUrl?: string;
  stockQuantity?: number;
}

export interface UpdateStockRequest {
  stock: number;
}