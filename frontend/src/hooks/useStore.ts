import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { Product, UIProduct, CartItem, Order, Category, ProductFilterParams, PersonalizationDetails } from '@/types/product';
import { apiClient, AuthResponse } from '@/lib/api';
import { convertProductToUI, convertProductsToUI, convertCategoriesToUI } from '@/lib/product-utils';
import { convertToNewFormat, calculateExtraCost } from '@/lib/personalization-utils';

interface StoreState {
  // Authentication
  currentUser: 'customer' | 'admin' | 'cashier' | null;
  userInfo: AuthResponse | null;
  setCurrentUser: (user: 'customer' | 'admin' | 'cashier' | null) => void;
  setUserInfo: (userInfo: AuthResponse | null) => void;
  login: (username: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;

  // Products (UI format for frontend compatibility)
  products: UIProduct[];
  categories: any[];
  setProducts: (products: UIProduct[]) => void;
  setCategories: (categories: any[]) => void;
  addProduct: (product: UIProduct) => void;
  updateProduct: (id: string, updates: Partial<UIProduct>) => void;
  deleteProduct: (id: string) => void;
  
  // Product API methods
  fetchProducts: (filters?: ProductFilterParams) => Promise<void>;
  fetchCategories: () => Promise<void>;
  fetchProduct: (productId: number) => Promise<UIProduct | null>;
  searchProducts: (query: string) => Promise<UIProduct[]>;
  updateProductAPI: (productId: number, productData: any) => Promise<void>;
  deleteProductAPI: (productId: number) => Promise<void>;

  // Cart
  cart: CartItem[];
  addToCart: (product: UIProduct, quantity?: number, personalizationDetails?: any, extraPrice?: number) => void;
  removeFromCart: (productId: string) => void;
  updateCartQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  getCartTotal: () => number;
  syncCartWithBackend: () => Promise<void>;
  forceSyncCart: () => Promise<void>;
  resetCartCompletely: () => void;
  forceClearCartState: () => void;

  // Orders
  orders: Order[];
  addOrder: (order: Order) => void;
  updateOrderStatus: (orderId: string, status: Order['status']) => void;
  checkout: (deliveryAddress: string, contactNumber: string, paymentMethod: 'credit_card' | 'payhear' | 'cod', cardDetails?: {
    cardNumber?: string;
    cardHolderName?: string;
    expiryMonth?: string;
    expiryYear?: string;
    cvv?: string;
  }) => Promise<string>;

  // Dashboard data
  dashboardStats: any;
  recentOrders: any[];
  topProducts: any[];
  allOrders: any[];
  ordersTotalPages: number;
  ordersCurrentPage: number;
  
  // Dashboard methods
  fetchDashboardStats: () => Promise<void>;
  fetchRecentOrders: (limit?: number) => Promise<void>;
  fetchTopProducts: (limit?: number) => Promise<void>;
  fetchAllOrders: (page?: number, size?: number) => Promise<void>;
  updateOrderStatusAPI: (orderId: number, status: string) => Promise<void>;

  // POS
  posCart: CartItem[];
  addToPosCart: (product: UIProduct, quantity?: number) => void;
  removeFromPosCart: (productId: string) => void;
  updatePosCartQuantity: (productId: string, quantity: number) => void;
  clearPosCart: () => void;
  getPosCartTotal: () => number;
}

// No more mock data - we load real data from the API

export const useStore = create<StoreState>()(
  persist(
    (set, get) => ({
      // Authentication
      currentUser: null,
      userInfo: null,
      setCurrentUser: (user) => set({ currentUser: user }),
      setUserInfo: (userInfo) => set({ userInfo }),
      login: async (username: string, password: string) => {
        try {
          const response = await apiClient.login({ username, password });
          const userRole = response.role.toLowerCase() as 'customer' | 'admin' | 'cashier';
          set({ 
            currentUser: userRole, 
            userInfo: response 
          });
        } catch (error) {
          console.error('Login failed:', error);
          throw error;
        }
      },
      register: async (username: string, email: string, password: string) => {
        try {
          const response = await apiClient.register({ username, email, password });
          const userRole = response.role.toLowerCase() as 'customer' | 'admin' | 'cashier';
          set({ 
            currentUser: userRole, 
            userInfo: response 
          });
        } catch (error) {
          console.error('Registration failed:', error);
          throw error;
        }
      },
      logout: () => {
        apiClient.logout();
        set({ currentUser: null, userInfo: null, cart: [] });
      },

      // Products - start with empty arrays, load real data from API
      products: [],
      categories: [],
      setProducts: (products) => set({ products }),
      setCategories: (categories) => set({ categories }),
      addProduct: (product) => set((state) => ({ 
        products: [...state.products, product] 
      })),
      updateProduct: (id, updates) => set((state) => ({
        products: state.products.map(p => p.id === id ? { ...p, ...updates } : p)
      })),
      deleteProduct: (id) => set((state) => ({
        products: state.products.filter(p => p.id !== id)
      })),

      // Product API methods
      fetchProducts: async (filters) => {
        try {
          console.log('🔄 Fetching products with filters:', filters);
          
          // For customers, always use getProducts() to avoid 403 errors
          // For admin users, use filterProducts if filters are provided
          const { currentUser } = get();
          let backendResponse;
          
          if (currentUser === 'admin' && filters && Object.keys(filters).length > 0) {
            console.log('🔄 Admin user with filters, using filterProducts');
            backendResponse = await apiClient.filterProducts(filters);
          } else {
            console.log('🔄 Customer user or no filters, fetching all products');
            backendResponse = await apiClient.getProducts();
          }
          
          console.log('✅ Backend response received:', backendResponse);
          
          // Handle paginated response - extract products from content array
          let backendProducts;
          if (Array.isArray(backendResponse)) {
            // Direct array response
            backendProducts = backendResponse;
          } else if (backendResponse && backendResponse.content && Array.isArray(backendResponse.content)) {
            // Paginated response - extract content
            backendProducts = backendResponse.content;
            console.log('🔄 Extracted products from paginated response:', backendProducts.length, 'products');
          } else {
            console.error('❌ Unexpected response format:', backendResponse);
            throw new Error('Unexpected API response format');
          }
          
          const uiProducts = convertProductsToUI(backendProducts);
          console.log('🔄 Converted to UI products:', uiProducts);
          set({ products: uiProducts });
          console.log('✅ Products set in store:', uiProducts.length, 'products');
        } catch (error) {
          console.error('❌ Failed to fetch products:', error);
          console.log('🔄 API failed, using empty array instead of mock data');
          // Don't use mock data, keep empty array
          set({ products: [] });
        }
      },

      fetchCategories: async () => {
        try {
          console.log('🔄 Fetching categories...');
          const backendCategories = await apiClient.getCategories();
          console.log('✅ Backend categories received:', backendCategories);
          const uiCategories = convertCategoriesToUI(backendCategories);
          console.log('🔄 Converted to UI categories:', uiCategories);
          set({ categories: uiCategories });
          console.log('✅ Categories set in store:', uiCategories.length, 'categories');
        } catch (error) {
          console.error('❌ Failed to fetch categories:', error);
          console.log('🔄 API failed, using empty array instead of mock data');
          // Don't use mock data, keep empty array
          set({ categories: [] });
        }
      },

      fetchProduct: async (productId) => {
        try {
          const backendProduct = await apiClient.getProduct(productId);
          return convertProductToUI(backendProduct);
        } catch (error) {
          console.error('Failed to fetch product:', error);
          return null;
        }
      },

      searchProducts: async (query) => {
        try {
          const backendProducts = await apiClient.searchProducts({ name: query });
          return convertProductsToUI(backendProducts);
        } catch (error) {
          console.error('Failed to search products:', error);
          // Fallback to local search
          const { products } = get();
          return products.filter(p => 
            p.name.toLowerCase().includes(query.toLowerCase()) ||
            p.description.toLowerCase().includes(query.toLowerCase())
          );
        }
      },

      updateProductAPI: async (productId, productData) => {
        try {
          console.log('🔄 Updating product:', productId, productData);
          const result = await apiClient.updateProduct(productId, productData);
          console.log('✅ Product updated successfully:', result);
          // Refresh the products list
          await get().fetchProducts();
        } catch (error) {
          console.error('Failed to update product:', error);
          throw error;
        }
      },

      deleteProductAPI: async (productId) => {
        try {
          console.log('🔄 Deleting product:', productId);
          await apiClient.deleteProduct(productId);
          console.log('✅ Product deleted successfully');
          // Refresh the products list
          await get().fetchProducts();
        } catch (error) {
          console.error('Failed to delete product:', error);
          throw error;
        }
      },

      // Cart
      cart: [],
      addToCart: async (product, quantity = 1, personalizationDetails = null, extraPrice = 0) => {
        const { currentUser } = get();
        console.log('🔄 addToCart called:', { productId: product.id, quantity, currentUser, personalizationDetails, extraPrice });
        console.log('🔄 Current user status:', currentUser ? 'Logged in as ' + currentUser.username : 'Not logged in');
        console.log('🔄 Auth token:', localStorage.getItem('auth_token') ? 'Present' : 'Missing');
        
        // Require authentication for cart operations
        if (!currentUser) {
          console.error('❌ Cannot add to cart - user not authenticated');
          alert('You must be logged in to add items to cart. Please log in first.');
          throw new Error('You must be logged in to add items to cart. Please log in first.');
        }
        
        console.log('✅ User authenticated, proceeding to add to cart');
        
        // Ensure personalization details are in the new format
        let normalizedPersonalizationDetails = personalizationDetails;
        if (personalizationDetails && Object.keys(personalizationDetails).length > 0) {
          // Check if it's in legacy format and convert if needed
          if (!personalizationDetails.customization_id && !personalizationDetails.teddy?.included) {
            normalizedPersonalizationDetails = convertToNewFormat(personalizationDetails);
            extraPrice = calculateExtraCost(normalizedPersonalizationDetails);
          }
        }
        
        const cartItem: CartItem = {
          ...product,
          quantity,
          personalizationDetails: normalizedPersonalizationDetails,
          extraPrice,
          totalPrice: (product.price + extraPrice) * quantity,
          // Ensure all required fields are present
          description: product.description || '',
          stock: product.stock || 999,
          rating: product.rating || 0,
          reviews: product.reviews || 0
        };
        
        try {
          console.log('🔄 Calling backend API to add to cart...');
          // Use the new API method if personalization details are provided
          if (normalizedPersonalizationDetails && Object.keys(normalizedPersonalizationDetails).length > 0) {
            console.log('🔄 Using addToCartWithPersonalization');
            await apiClient.addToCartWithPersonalization(
              parseInt(product.id), 
              quantity, 
              normalizedPersonalizationDetails
            );
          } else {
            console.log('🔄 Using addToCart with productId:', parseInt(product.id), 'quantity:', quantity);
            await apiClient.addToCart(parseInt(product.id), quantity, normalizedPersonalizationDetails);
          }
          console.log('✅ Backend API call successful');
            
          // Wait a moment for backend to process, then sync to get backend IDs
          console.log('🔄 Waiting for backend to process...');
          await new Promise(resolve => setTimeout(resolve, 500));
          
          console.log('🔄 Syncing cart with backend to get backend IDs...');
          await get().syncCartWithBackend();
          console.log('✅ Cart synced with backend');
          console.log('✅ Item added to cart successfully');
        } catch (error) {
          console.error('❌ Failed to add to cart:', error);
          alert(`❌ Failed to add to cart: ${error instanceof Error ? error.message : 'Unknown error'}`);
          throw error;
        }
      },
      removeFromCart: async (productId) => {
        const { currentUser, cart } = get();
        console.log('🔄 removeFromCart called:', { productId, currentUser });
        console.log('🔄 Current cart items:', cart);
        console.log('🔄 Looking for item with ID:', productId);
        
        // Find the cart item to get its backend ID
        let cartItem = get().cart.find(item => item.id === productId);
        console.log('🔄 Found cart item:', cartItem);
        
        if (currentUser) {
          try {
            // If no backendId, sync with backend first to get it
            if (cartItem && (!('backendId' in cartItem) || !cartItem.backendId)) {
              console.log('🔄 No backendId found, syncing with backend first...');
              await get().syncCartWithBackend();
              // Re-find the cart item after sync
              cartItem = get().cart.find(item => item.id === productId);
              console.log('🔄 Cart item after sync:', cartItem);
            }
            
            if (cartItem && 'backendId' in cartItem && cartItem.backendId) {
              console.log('🔄 Using backend ID for removal:', cartItem.backendId);
              await apiClient.removeFromCart((cartItem as any).backendId);
              console.log('✅ Backend removal successful');
              
              // Wait a moment then sync to ensure consistency
              await new Promise(resolve => setTimeout(resolve, 300));
              await get().syncCartWithBackend();
              console.log('✅ Cart synced after removal');
            } else {
              console.log('⚠️ Still no backendId found after sync, removing locally only');
              // Remove locally
              set((state) => ({
                cart: state.cart.filter(item => item.id !== productId)
              }));
            }
          } catch (error) {
            console.error('Failed to remove from backend:', error);
            console.log('🔄 Removing locally despite backend error');
            set((state) => ({
              cart: state.cart.filter(item => item.id !== productId)
            }));
          }
        } else {
          // Not authenticated, remove locally
          console.log('🔄 User not authenticated, removing locally');
          set((state) => ({
            cart: state.cart.filter(item => item.id !== productId)
          }));
        }
        console.log('✅ Item removal complete');
      },
      updateCartQuantity: async (productId, quantity) => {
        const { currentUser } = get();
        console.log('🔄 updateCartQuantity called:', { productId, quantity, currentUser });
        
        if (currentUser) {
          try {
            let cartItem = get().cart.find(item => item.id === productId);
            console.log('🔄 Found cart item:', cartItem);
            
            // If no backendId, sync with backend first to get it
            if (cartItem && (!('backendId' in cartItem) || !cartItem.backendId)) {
              console.log('🔄 No backendId found, syncing with backend first...');
              await get().syncCartWithBackend();
              // Re-find the cart item after sync
              cartItem = get().cart.find(item => item.id === productId);
              console.log('🔄 Cart item after sync:', cartItem);
            }
            
            if (cartItem && 'backendId' in cartItem && cartItem.backendId) {
              console.log('🔄 Using backend ID for update:', cartItem.backendId);
              await apiClient.updateCartItem((cartItem as any).backendId, quantity);
              console.log('✅ Backend update successful');
              
              // Wait a moment then sync to ensure consistency
              await new Promise(resolve => setTimeout(resolve, 300));
              await get().syncCartWithBackend();
              console.log('✅ Cart synced after quantity update');
            } else {
              console.log('⚠️ Still no backendId found after sync, updating locally only');
              // If no backendId, just update locally
              set((state) => ({
                cart: quantity <= 0 
                  ? state.cart.filter(item => item.id !== productId)
                  : state.cart.map(item =>
                      item.id === productId ? { ...item, quantity } : item
                    )
              }));
            }
          } catch (error) {
            console.error('Failed to update cart quantity:', error);
            // Fallback to local update
            set((state) => ({
              cart: quantity <= 0 
                ? state.cart.filter(item => item.id !== productId)
                : state.cart.map(item =>
                    item.id === productId ? { ...item, quantity } : item
                  )
            }));
          }
        } else {
          console.log('🔄 User not authenticated, updating locally');
          // For unauthenticated users, just update locally
          set((state) => ({
            cart: quantity <= 0 
              ? state.cart.filter(item => item.id !== productId)
              : state.cart.map(item =>
                  item.id === productId ? { ...item, quantity } : item
                )
          }));
        }
      },
      clearCart: async () => {
        const { currentUser } = get();
        console.log('🔄 clearCart called:', { currentUser, cartLength: get().cart.length });
        
        if (currentUser) {
          try {
            await apiClient.clearCart();
            console.log('✅ Backend cart cleared successfully');
          } catch (error) {
            console.error('Failed to clear cart:', error);
          }
        }
        // Always clear the local cart regardless of authentication status
        set({ cart: [] });
        
        // Force clear from localStorage as well
        try {
          const storageKey = 'useStore';
          const stored = localStorage.getItem(storageKey);
          if (stored) {
            const parsed = JSON.parse(stored);
            parsed.state.cart = [];
            localStorage.setItem(storageKey, JSON.stringify(parsed));
            console.log('✅ Cart cleared from localStorage');
          }
        } catch (error) {
          console.error('Failed to clear cart from localStorage:', error);
        }
        
        console.log('✅ Local cart cleared, new cart length:', get().cart.length);
      },
      createNewCart: async () => {
        const { currentUser } = get();
        console.log('🔄 createNewCart called:', { currentUser });
        
        // Clear the current cart completely
        await get().clearCart();
        
        // Also call resetCartCompletely to ensure localStorage is cleared
        get().resetCartCompletely();
        
        if (currentUser) {
          try {
            // For authenticated users, call the backend to create a new cart
            await apiClient.createNewCart();
            console.log('✅ New cart created in backend for authenticated user');
          } catch (error) {
            console.error('Failed to create new cart:', error);
          }
        } else {
          // For unauthenticated users, the session cart is already cleared
          console.log('✅ New session cart created for unauthenticated user');
        }
        
        console.log('✅ New cart created successfully');
      },
      resetCartCompletely: () => {
        console.log('🔄 resetCartCompletely called');
        
        // Clear local state
        set({ cart: [] });
        
        // Clear from localStorage
        try {
          const storageKey = 'useStore';
          const stored = localStorage.getItem(storageKey);
          if (stored) {
            const parsed = JSON.parse(stored);
            parsed.state.cart = [];
            localStorage.setItem(storageKey, JSON.stringify(parsed));
            console.log('✅ Cart completely reset from localStorage');
          }
        } catch (error) {
          console.error('Failed to reset cart from localStorage:', error);
        }
        
        console.log('✅ Cart completely reset');
      },
      forceClearCartState: () => {
        console.log('🔄 forceClearCartState called');
        
        // Force clear cart state immediately
        set({ cart: [] });
        
        // Clear from localStorage
        try {
          const storageKey = 'useStore';
          const stored = localStorage.getItem(storageKey);
          if (stored) {
            const parsed = JSON.parse(stored);
            parsed.state.cart = [];
            localStorage.setItem(storageKey, JSON.stringify(parsed));
            console.log('✅ Cart state force cleared');
          }
        } catch (error) {
          console.error('Failed to force clear cart state:', error);
        }
      },
      getCartTotal: () => {
        const { cart } = get();
        return cart.reduce((total, item) => {
          const basePrice = item.price * item.quantity;
          const extraPrice = (item.extraPrice || 0) * item.quantity;
          return total + basePrice + extraPrice;
        }, 0);
      },
      syncCartWithBackend: async () => {
        const { currentUser } = get();
        if (!currentUser) {
          console.log('🔄 syncCartWithBackend called but user not authenticated, skipping');
          return;
        }
        
        try {
          console.log('🔄 Syncing cart with backend...');
          const backendCart = await apiClient.getCartItems();
          console.log('🔄 Backend cart received:', backendCart);
          
          // Convert backend cart items to frontend format
          const frontendCart: CartItem[] = backendCart.map(item => ({
            id: item.productId.toString(),
            name: item.productName,
            description: item.productName, // Use product name as description
            price: item.productPrice,
            quantity: item.quantity,
            image: item.imageUrl || '/placeholder.svg',
            category: 'Unknown',
            size: 'Medium',
            stock: 999,
            rating: 0,
            reviews: 0,
            backendId: item.itemId, // Use itemId from backend response
            personalizationDetails: item.personalizationDetails,
            extraPrice: item.extraPrice || 0,
            totalPrice: item.itemTotal
          }));
          
          console.log('🔄 Converted frontend cart:', frontendCart);
          set({ cart: frontendCart });
          console.log('✅ Cart synced with backend, new cart length:', frontendCart.length);
        } catch (error) {
          console.error('Failed to sync cart with backend:', error);
        }
      },
      forceSyncCart: async () => {
        const { currentUser } = get();
        if (!currentUser) {
          console.log('🔄 forceSyncCart called but user not authenticated, skipping');
          return;
        }
        
        try {
          console.log('🔄 Force syncing cart with backend...');
          const backendCart = await apiClient.getCartItems();
          console.log('🔄 Backend cart received:', backendCart);
          
          // Convert backend cart items to frontend format
          const frontendCart: CartItem[] = backendCart.map(item => ({
            id: item.productId.toString(),
            name: item.productName,
            description: item.productName, // Use product name as description
            price: item.productPrice,
            quantity: item.quantity,
            image: item.imageUrl || '/placeholder.svg',
            category: 'Unknown',
            size: 'Medium',
            stock: 999,
            rating: 0,
            reviews: 0,
            backendId: item.itemId, // Use itemId from backend response
            personalizationDetails: item.personalizationDetails,
            extraPrice: item.extraPrice || 0,
            totalPrice: item.itemTotal
          }));
          
          console.log('🔄 Converted frontend cart:', frontendCart);
          set({ cart: frontendCart });
          console.log('✅ Cart force synced with backend, new cart length:', frontendCart.length);
        } catch (error) {
          console.error('Failed to force sync cart with backend:', error);
        }
      },

      // Orders
      orders: [],
      addOrder: (order) => set((state) => ({ 
        orders: [...state.orders, order] 
      })),
      updateOrderStatus: (orderId, status) => set((state) => ({
        orders: state.orders.map(order =>
          order.id === orderId ? { ...order, status } : order
        )
      })),
      checkout: async (deliveryAddress: string, contactNumber: string, paymentMethod: 'credit_card' | 'payhear' | 'cod', cardDetails?: {
        cardNumber?: string;
        cardHolderName?: string;
        expiryMonth?: string;
        expiryYear?: string;
        cvv?: string;
      }) => {
        const { currentUser, userInfo } = get();
        console.log('🔄 checkout called:', { currentUser, userInfo, deliveryAddress, contactNumber, paymentMethod });
        console.log('🔄 Auth token in localStorage:', localStorage.getItem('auth_token'));
        
        if (!currentUser) {
          throw new Error('You must be logged in to place an order');
        }
        
        try {
          // Sync cart with backend before checkout to ensure we have the latest cart items
          console.log('🔄 Syncing cart with backend before checkout...');
          await get().syncCartWithBackend();
          
          // Check cart state after sync
          const { cart } = get();
          console.log('🔄 Cart state after sync:', cart.length, 'items');
          if (cart.length === 0) {
            throw new Error('Your cart is empty. Please add items to your cart before checkout.');
          }
          
          const result = await apiClient.checkout({
            deliveryAddress,
            contactNumber,
            paymentMethod,
            ...cardDetails
          });
          // Don't create new cart here - let the checkout page handle clearing
          // await get().createNewCart();
          
          // Ensure result is a string
          return typeof result === 'string' ? result : 'Order placed successfully';
        } catch (error) {
          console.error('Checkout failed:', error);
          throw error;
        }
      },

      // Dashboard data
      dashboardStats: null,
      recentOrders: [],
      topProducts: [],
      allOrders: [],
      ordersTotalPages: 0,
      ordersCurrentPage: 0,

      // Dashboard methods
      fetchDashboardStats: async () => {
        try {
          console.log('🔄 Fetching dashboard stats...');
          const stats = await apiClient.getDashboardStats();
          console.log('✅ Dashboard stats received:', stats);
          set({ dashboardStats: stats });
        } catch (error) {
          console.error('❌ Failed to fetch dashboard stats:', error);
        }
      },

      fetchRecentOrders: async (limit = 10) => {
        try {
          console.log('🔄 Fetching recent orders...');
          const orders = await apiClient.getRecentOrders(limit);
          console.log('✅ Recent orders received:', orders);
          set({ recentOrders: orders });
        } catch (error) {
          console.error('❌ Failed to fetch recent orders:', error);
        }
      },

      fetchTopProducts: async (limit = 10) => {
        try {
          console.log('🔄 Fetching top products...');
          const products = await apiClient.getTopSellingProducts(limit);
          console.log('✅ Top products received:', products);
          set({ topProducts: products });
        } catch (error) {
          console.error('❌ Failed to fetch top products:', error);
        }
      },

      fetchAllOrders: async (page = 0, size = 10) => {
        try {
          console.log('🔄 Fetching all orders...', { page, size });
          const response = await apiClient.getAllOrders(page, size);
          console.log('✅ All orders received:', response);
          set({ 
            allOrders: response.content,
            ordersTotalPages: response.totalPages,
            ordersCurrentPage: response.number
          });
        } catch (error) {
          console.error('❌ Failed to fetch all orders:', error);
        }
      },

      updateOrderStatusAPI: async (orderId, status) => {
        try {
          console.log('🔄 Updating order status:', { orderId, status });
          await apiClient.updateOrderStatus(orderId, status);
          console.log('✅ Order status updated successfully');
          // Refresh orders after update
          await get().fetchAllOrders();
        } catch (error) {
          console.error('❌ Failed to update order status:', error);
          throw error;
        }
      },

      // POS
      posCart: [],
      addToPosCart: (product, quantity = 1) => set((state) => {
        const existingItem = state.posCart.find(item => item.id === product.id);
        if (existingItem) {
          return {
            posCart: state.posCart.map(item =>
              item.id === product.id
                ? { ...item, quantity: item.quantity + quantity }
                : item
            )
          };
        } else {
          return {
            posCart: [...state.posCart, { ...product, quantity }]
          };
        }
      }),
      removeFromPosCart: (productId) => set((state) => ({
        posCart: state.posCart.filter(item => item.id !== productId)
      })),
      updatePosCartQuantity: (productId, quantity) => set((state) => ({
        posCart: quantity <= 0 
          ? state.posCart.filter(item => item.id !== productId)
          : state.posCart.map(item =>
              item.id === productId ? { ...item, quantity } : item
            )
      })),
      clearPosCart: () => set({ posCart: [] }),
      getPosCartTotal: () => {
        const { posCart } = get();
        return posCart.reduce((total, item) => total + (item.price * item.quantity), 0);
      },
    }),
    {
      name: 'teddylove-store',
    }
  )
);