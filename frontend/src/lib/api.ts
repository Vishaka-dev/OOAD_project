import {
  Category,
  Product,
  ProductFilterParams,
  ProductSearchParams,
  PriceRangeParams,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CreateProductRequest,
  UpdateProductRequest,
  UpdateStockRequest
} from '@/types/product';

const API_BASE_URL = 'pinkypromisebackend-production.up.railway.ap';

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
  itemId: number;
  productId: number;
  productName: string;
  productPrice: number;
  imageUrl?: string;
  quantity: number;
  personalizationDetails?: Record<string, unknown>;
  extraPrice?: number;
  itemTotal?: number;
}

export interface CheckoutRequest {
  deliveryAddress: string;
  contactNumber: string;
  paymentMethod: 'credit_card' | 'payhear' | 'cod';
  // Optional fields for credit card payments
  cardNumber?: string;
  cardHolderName?: string;
  expiryMonth?: string;
  expiryYear?: string;
  cvv?: string;
}

export interface OrderResponse {
  orderId: number;
  customerName: string;
  customerEmail: string;
  totalPrice: number;
  status: string;
  orderDate: string;
  orderItems: unknown[];
}

// Dashboard interfaces
export interface AdminDashboardStatsDTO {
  totalOrders: number;
  pendingOrders: number;
  confirmedOrders: number;
  shippedOrders: number;
  deliveredOrders: number;
  cancelledOrders: number;
  totalRevenue: number;
  todayRevenue: number;
  weeklyRevenue: number;
  monthlyRevenue: number;
  totalCustomers: number;
  totalProducts: number;
  lowStockProducts: number;
  outOfStockProducts: number;
  activeProducts: number;
}

export interface OrderSummaryDTO {
  orderId: number;
  customerName: string;
  orderDate: string;
  status: string;
  totalPrice: number;
  itemCount: number;
}

export interface ProductSalesDTO {
  productId: number;
  productName: string;
  categoryName: string;
  quantitySold: number;
  totalRevenue: number;
  averagePrice: number;
}

export interface SalesReportDTO {
  date: string;
  orderCount: number;
  totalSales: number;
  averageOrderValue: number;
}

export interface PaginatedOrderResponse {
  content: OrderResponse[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
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
    };

    // Always check localStorage for the latest token
    const currentToken = localStorage.getItem('auth_token');
    if (currentToken) {
      headers.Authorization = `Bearer ${currentToken}`;
    }

    // Merge with any existing headers
    if (options.headers) {
      Object.assign(headers, options.headers);
    }

    const config: RequestInit = {
      ...options,
      headers,
    };

    console.log('🔄 Making API request:', { url, method: options.method || 'GET', headers });
    console.log('🔄 Auth token being sent:', currentToken);

    try {
      const response = await fetch(url, config);
      console.log('📡 API response received:', {
        url,
        status: response.status,
        ok: response.ok,
        headers: Object.fromEntries(response.headers.entries())
      });

      if (!response.ok) {
        const errorData = await response.text();
        console.error('❌ API request failed:', { url, status: response.status, error: errorData });
        throw new Error(errorData || `HTTP error! status: ${response.status}`);
      }

      // Handle empty responses
      const text = await response.text();
      if (!text) {
        console.log('⚠️ Empty response received for:', url);
        return {} as T;
      }

      // Try to parse as JSON, but handle non-JSON responses gracefully
      try {
        const data = JSON.parse(text);
        console.log('✅ API data parsed successfully:', { url, dataLength: Array.isArray(data) ? data.length : 'not array' });
        return data;
      } catch (parseError) {
        // If it's not JSON, return the text as a simple object
        console.log('⚠️ Non-JSON response received for:', url, 'text:', text);
        return { message: text } as T;
      }
    } catch (error) {
      console.error('❌ API request failed:', { url, error });
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

  async addToCart(productId: number, quantity: number, personalizationDetails?: Record<string, unknown>): Promise<void> {
    const params = new URLSearchParams({
      productId: productId.toString(),
      quantity: quantity.toString(),
    });

    await this.request(`/cart/add?${params}`, {
      method: 'POST',
      body: personalizationDetails ? JSON.stringify(personalizationDetails) : undefined,
    });
  }

  // New method for adding to cart with structured personalization
  async addToCartWithPersonalization(productId: number, quantity: number, personalization?: Record<string, unknown>): Promise<void> {
    const requestBody = {
      productId,
      quantity,
      personalization
    };

    await this.request('/cart/add-with-personalization', {
      method: 'POST',
      body: JSON.stringify(requestBody),
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

  async createNewCart(): Promise<void> {
    await this.request('/cart/new', {
      method: 'POST',
    });
  }

  // Order methods
  async checkout(checkoutData: CheckoutRequest): Promise<string> {
    const response = await this.request<string | { message: string }>('/orders/checkout', {
      method: 'POST',
      body: JSON.stringify(checkoutData),
    });

    // Handle both string and object responses
    if (typeof response === 'string') {
      return response;
    } else if (response && typeof response === 'object' && 'message' in response) {
      return response.message;
    } else {
      return 'Order placed successfully';
    }
  }

  async getMyOrders(): Promise<OrderResponse[]> {
    return this.request<OrderResponse[]>('/orders/my-orders');
  }

  async getOrder(orderId: number): Promise<OrderResponse> {
    return this.request<OrderResponse>(`/orders/${orderId}`);
  }

  // Member A - Product Catalog API Methods

  // Category methods
  async getCategories(): Promise<Category[]> {
    return this.request<Category[]>('/categories');
  }

  async getCategory(categoryId: number): Promise<Category> {
    return this.request<Category>(`/categories/${categoryId}`);
  }

  async getCategoriesWithProducts(): Promise<Category[]> {
    return this.request<Category[]>('/categories/with-products');
  }

  // Product methods
  async getProducts(): Promise<Product[]> {
    return this.request<Product[]>('/products');
  }

  async getProduct(productId: number): Promise<Product> {
    return this.request<Product>(`/products/${productId}`);
  }

  async getProductsByCategory(categoryId: number): Promise<Product[]> {
    return this.request<Product[]>(`/products/category/${categoryId}`);
  }

  async searchProducts(params: ProductSearchParams): Promise<Product[]> {
    const searchParams = new URLSearchParams();
    searchParams.append('name', params.name);
    if (params.page) searchParams.append('page', params.page.toString());
    if (params.size) searchParams.append('size', params.size.toString());

    return this.request<Product[]>(`/products/search?${searchParams}`);
  }

  async getInStockProducts(): Promise<Product[]> {
    return this.request<Product[]>('/products/in-stock');
  }

  async getProductsByPriceRange(params: PriceRangeParams): Promise<Product[]> {
    const searchParams = new URLSearchParams();
    searchParams.append('minPrice', params.minPrice.toString());
    searchParams.append('maxPrice', params.maxPrice.toString());
    if (params.page) searchParams.append('page', params.page.toString());
    if (params.size) searchParams.append('size', params.size.toString());

    return this.request<Product[]>(`/products/price-range?${searchParams}`);
  }

  async filterProducts(params: ProductFilterParams): Promise<Product[]> {
    const searchParams = new URLSearchParams();
    if (params.name) searchParams.append('name', params.name);
    if (params.categoryId) searchParams.append('categoryId', params.categoryId.toString());
    if (params.minPrice) searchParams.append('minPrice', params.minPrice.toString());
    if (params.maxPrice) searchParams.append('maxPrice', params.maxPrice.toString());
    if (params.inStock !== undefined) searchParams.append('inStock', params.inStock.toString());
    if (params.page) searchParams.append('page', params.page.toString());
    if (params.size) searchParams.append('size', params.size.toString());

    return this.request<Product[]>(`/products/filter?${searchParams}`);
  }

  // Admin Category methods
  async createCategory(categoryData: CreateCategoryRequest): Promise<Category> {
    return this.request<Category>('/categories/admin', {
      method: 'POST',
      body: JSON.stringify(categoryData),
    });
  }

  async updateCategory(categoryId: number, categoryData: UpdateCategoryRequest): Promise<Category> {
    return this.request<Category>(`/categories/admin/${categoryId}`, {
      method: 'PUT',
      body: JSON.stringify(categoryData),
    });
  }

  async deleteCategory(categoryId: number): Promise<void> {
    await this.request<void>(`/categories/admin/${categoryId}`, {
      method: 'DELETE',
    });
  }

  // Admin Product methods
  async createProduct(productData: CreateProductRequest): Promise<Product> {
    return this.request<Product>('/products/admin', {
      method: 'POST',
      body: JSON.stringify(productData),
    });
  }

  async updateProduct(productId: number, productData: UpdateProductRequest): Promise<Product> {
    console.log('🔄 API Client - Updating product:', productId, productData);
    return this.request<Product>(`/products/admin/${productId}`, {
      method: 'PUT',
      body: JSON.stringify(productData),
    });
  }

  async deleteProduct(productId: number): Promise<void> {
    await this.request<void>(`/products/admin/${productId}`, {
      method: 'DELETE',
    });
  }

  async updateProductStock(productId: number, stock: number): Promise<Product> {
    const params = new URLSearchParams({ stock: stock.toString() });
    return this.request<Product>(`/products/admin/${productId}/stock?${params}`, {
      method: 'PUT',
    });
  }

  async getLowStockProducts(threshold: number = 10): Promise<Product[]> {
    const params = new URLSearchParams({ threshold: threshold.toString() });
    return this.request<Product[]>(`/products/admin/low-stock?${params}`);
  }

  // Dashboard methods
  async getDashboardStats(): Promise<AdminDashboardStatsDTO> {
    return this.request<AdminDashboardStatsDTO>('/admin/dashboard/stats');
  }

  async getSalesReport(startDate: string, endDate: string): Promise<SalesReportDTO[]> {
    const params = new URLSearchParams({
      startDate,
      endDate,
    });
    return this.request<SalesReportDTO[]>(`/admin/dashboard/sales-report?${params}`);
  }

  async getTopSellingProducts(limit: number = 10): Promise<ProductSalesDTO[]> {
    const params = new URLSearchParams({ limit: limit.toString() });
    return this.request<ProductSalesDTO[]>(`/admin/dashboard/top-products?${params}`);
  }

  async getRecentOrders(limit: number = 10): Promise<OrderSummaryDTO[]> {
    const params = new URLSearchParams({ limit: limit.toString() });
    return this.request<OrderSummaryDTO[]>(`/admin/dashboard/recent-orders?${params}`);
  }

  async getAllOrders(page: number = 0, size: number = 10): Promise<PaginatedOrderResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    return this.request<PaginatedOrderResponse>(`/orders/admin/all?${params}`);
  }

  async updateOrderStatus(orderId: number, status: string): Promise<void> {
    return this.request(`/orders/admin/${orderId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  // Personalization methods
  async getPersonalizationOptions(productId: number): Promise<any[]> {
    return this.request<any[]>(`/personalization/products/${productId}/options`);
  }

  async createPersonalizationOption(productId: number, optionData: any): Promise<any> {
    return this.request<any>(`/personalization/products/${productId}/options`, {
      method: 'POST',
      body: JSON.stringify(optionData),
    });
  }

  async updatePersonalizationOption(optionId: number, optionData: any): Promise<any> {
    return this.request<any>(`/personalization/options/${optionId}`, {
      method: 'PUT',
      body: JSON.stringify(optionData),
    });
  }

  async deletePersonalizationOption(optionId: number): Promise<void> {
    return this.request(`/personalization/options/${optionId}`, {
      method: 'DELETE',
    });
  }

  async addPersonalizedToCart(request: any): Promise<any> {
    return this.request<any>('/personalization/add-to-cart', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }
}

// Create and export a singleton instance
export const apiClient = new ApiClient(API_BASE_URL);
