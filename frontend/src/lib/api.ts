const API_BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL || '/api';

// Types for API requests and responses
export interface AuthRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  role: string;
}

export interface CartItemDTO {
  id?: number; // backend uses itemId; map later
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  personalizationDetails?: any;
  customizationId?: string;
}

export interface CheckoutRequest {
  customerName: string;
  customerEmail: string;
  deliveryAddress: string;
  contactNumber: string;
  paymentMethod: string;
  cartItems?: CartItemCheckoutDTO[];
}

export interface CartItemCheckoutDTO {
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  personalizationDetails?: any;
  customizationId?: string;
}

export interface OrderResponse {
  orderId: number;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  status: string;
  orderDate: string;
  items: any[];
}

export interface PersonalizationOptionDTO {
  optionId: number;
  productId: number;
  usiType?: string;
  massage?: string;
  color?: string;
  extraPrice?: number;
  maxLength?: number;
}

class ApiClient {
  private baseURL: string;
  private token: string | null = null;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
    this.token = localStorage.getItem('auth_token');
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> | undefined),
    };

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }

    const config: RequestInit = {
      ...options,
      headers,
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || `HTTP error! status: ${response.status}`);
      }

      // Handle empty responses
      const text = await response.text();
      if (!text) {
        return {} as T;
      }

      return JSON.parse(text);
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Authentication methods
  async login(credentials: AuthRequest): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    
    this.token = response.token;
    localStorage.setItem('auth_token', response.token);
    return response;
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
    
    this.token = response.token;
    localStorage.setItem('auth_token', response.token);
    return response;
  }

  logout() {
    this.token = null;
    localStorage.removeItem('auth_token');
  }

  isAuthenticated(): boolean {
    return !!this.token;
  }

  // Cart methods
  async getCartItems(): Promise<CartItemDTO[]> {
    return this.request<CartItemDTO[]>('/cart');
  }

  async getCartTotal(): Promise<number> {
    const total = await this.request<number>('/cart/total');
    return total;
  }

  async addToCart(productId: number, quantity: number, personalizationDetails?: any): Promise<void> {
    const params = new URLSearchParams({
      productId: productId.toString(),
      quantity: quantity.toString(),
    });

    await this.request(`/cart/add?${params}`, {
      method: 'POST',
      body: personalizationDetails ? JSON.stringify(personalizationDetails) : undefined,
    });
  }

  async addPersonalizedToCart(payload: {
    productId: number;
    quantity: number;
    usiType?: string;
    massage?: string;
    color?: string;
    extraPrice?: number;
    maxLength?: number;
    additionalDetails?: any;
  }): Promise<CartItemDTO> {
    return this.request<CartItemDTO>('/personalization/add-to-cart', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async updateCartItem(itemId: number, quantity: number): Promise<void> {
    const params = new URLSearchParams({
      quantity: quantity.toString(),
    });

    await this.request(`/cart/update/${itemId}?${params}`, {
      method: 'PUT',
    });
  }

  async removeFromCart(itemId: number): Promise<void> {
    await this.request(`/cart/remove/${itemId}`, {
      method: 'DELETE',
    });
  }

  async clearCart(): Promise<void> {
    await this.request('/cart/clear', {
      method: 'DELETE',
    });
  }

  // Order methods
  async checkout(checkoutData: CheckoutRequest): Promise<string> {
    return this.request<string>('/orders/checkout', {
      method: 'POST',
      body: JSON.stringify(checkoutData),
    });
  }

  async getMyOrders(): Promise<OrderResponse[]> {
    return this.request<OrderResponse[]>('/orders/my-orders');
  }

  async getOrder(orderId: number): Promise<OrderResponse> {
    return this.request<OrderResponse>(`/orders/${orderId}`);
  }

  // Personalization methods
  async getPersonalizationOptions(productId: number): Promise<PersonalizationOptionDTO[]> {
    return this.request<PersonalizationOptionDTO[]>(`/personalization/products/${productId}/options`);
  }

  // Product methods
  async getProducts(): Promise<any[]> {
    return this.request<any[]>(`/products`);
  }
}

// Create and export a singleton instance
export const apiClient = new ApiClient(API_BASE_URL);

