import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { Product, CartItem, Order } from '@/types/product';
import { apiClient, AuthResponse } from '@/lib/api';

interface StoreState {
  // Authentication
  currentUser: 'customer' | 'admin' | 'cashier' | null;
  userInfo: AuthResponse | null;
  setCurrentUser: (user: 'customer' | 'admin' | 'cashier' | null) => void;
  setUserInfo: (userInfo: AuthResponse | null) => void;
  login: (username: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;

  // Products
  products: Product[];
  setProducts: (products: Product[]) => void;
  loadProducts: () => Promise<void>;
  addProduct: (product: Product) => void;
  updateProduct: (id: string, updates: Partial<Product>) => void;
  deleteProduct: (id: string) => void;

  // Cart
  cart: CartItem[];
  addToCart: (product: Product, quantity?: number, personalizationDetails?: any) => void;
  removeFromCart: (productId: string) => void;
  updateCartQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  getCartTotal: () => number;
  syncCartWithBackend: () => Promise<void>;

  // Orders
  orders: Order[];
  addOrder: (order: Order) => void;
  updateOrderStatus: (orderId: string, status: Order['status']) => void;
  checkout: (customerName: string, customerEmail: string, deliveryAddress: string, contactNumber: string, paymentMethod: string) => Promise<string>;

  // POS
  posCart: CartItem[];
  addToPosCart: (product: Product, quantity?: number) => void;
  removeFromPosCart: (productId: string) => void;
  updatePosCartQuantity: (productId: string, quantity: number) => void;
  clearPosCart: () => void;
  getPosCartTotal: () => number;
}

// Mock product data
const mockProducts: Product[] = [
  {
    id: '1',
    name: 'Classic Brown Teddy',
    description: 'A timeless brown teddy bear perfect for cuddling',
    price: 29.99,
    image: '/placeholder.svg',
    category: 'Classic',
    size: 'Medium',
    stock: 15,
    featured: true,
    rating: 4.8,
    reviews: 124
  },
  {
    id: '2',
    name: 'Pink Princess Bear',
    description: 'Adorable pink teddy with a sparkly crown',
    price: 34.99,
    originalPrice: 39.99,
    image: '/placeholder.svg',
    category: 'Princess',
    size: 'Large',
    stock: 8,
    featured: true,
    rating: 4.9,
    reviews: 89
  },
  {
    id: '3',
    name: 'Tiny Pocket Bear',
    description: 'Perfect small companion for on-the-go adventures',
    price: 12.99,
    image: '/placeholder.svg',
    category: 'Mini',
    size: 'Small',
    stock: 25,
    rating: 4.6,
    reviews: 156
  },
  {
    id: '4',
    name: 'Giant Cuddle Bear',
    description: 'Extra large teddy for the ultimate cuddle experience',
    price: 89.99,
    image: '/placeholder.svg',
    category: 'Giant',
    size: 'Extra Large',
    stock: 3,
    featured: true,
    rating: 5.0,
    reviews: 45
  },
  {
    id: '5',
    name: 'Cream Vanilla Bear',
    description: 'Soft cream-colored teddy with vanilla scent',
    price: 27.99,
    image: '/placeholder.svg',
    category: 'Scented',
    size: 'Medium',
    stock: 12,
    rating: 4.7,
    reviews: 78
  },
  {
    id: '6',
    name: 'Adventure Explorer Bear',
    description: 'Comes with hat and backpack for adventures',
    price: 42.99,
    image: '/placeholder.svg',
    category: 'Adventure',
    size: 'Large',
    stock: 6,
    rating: 4.8,
    reviews: 67
  }
];

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
      register: async (name: string, email: string, password: string) => {
        try {
          const response = await apiClient.register({ username: name, email, password });
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

      // Products
      products: [],
      setProducts: (products) => set({ products }),
      loadProducts: async () => {
        try {
          const backendProducts = await apiClient.getProducts();
          if (!backendProducts || backendProducts.length === 0) {
            set({ products: mockProducts });
            return;
          }
          const mapped: Product[] = backendProducts.map((p: any) => ({
            id: String(p.productId),
            name: p.name,
            description: p.description || '',
            price: Number(p.price || 0),
            image: p.imageUrl || '/placeholder.svg',
            category: p.categoryName || 'Unknown',
            size: 'Medium',
            stock: Number(p.stockQuantity ?? 0),
            featured: false,
            rating: 0,
            reviews: 0,
          }));
          set({ products: mapped });
        } catch (e) {
          console.error('Failed to load products from backend, falling back to mock.', e);
          set({ products: mockProducts });
        }
      },
      addProduct: (product) => set((state) => ({ 
        products: [...state.products, product] 
      })),
      updateProduct: (id, updates) => set((state) => ({
        products: state.products.map(p => p.id === id ? { ...p, ...updates } : p)
      })),
      deleteProduct: (id) => set((state) => ({
        products: state.products.filter(p => p.id !== id)
      })),

      // Cart
      cart: [],
      addToCart: async (product, quantity = 1, personalizationDetails) => {
        const { currentUser } = get();
        if (currentUser) {
          try {
            if (personalizationDetails && Object.keys(personalizationDetails).length > 0) {
              // Map a few common fields if present; others go in additionalDetails
              const { usiType, massage, color, extraPrice, maxLength, ...rest } = personalizationDetails || {};
              await apiClient.addPersonalizedToCart({
                productId: parseInt(product.id),
                quantity,
                usiType,
                massage,
                color,
                extraPrice,
                maxLength,
                additionalDetails: rest,
              });
            } else {
              await apiClient.addToCart(parseInt(product.id), quantity);
            }
            // Sync with backend after adding
            await get().syncCartWithBackend();
          } catch (error) {
            console.error('Failed to add to cart:', error);
            // Fallback to local cart if API fails
            set((state) => {
              const existingItem = state.cart.find(item => item.id === product.id);
              if (existingItem) {
                return {
                  cart: state.cart.map(item =>
                    item.id === product.id
                      ? { ...item, quantity: item.quantity + quantity, personalizationDetails }
                      : item
                  )
                };
              } else {
                return {
                  cart: [...state.cart, { ...product, quantity, personalizationDetails }]
                };
              }
            });
          }
        } else {
          // Local cart for non-authenticated users
          set((state) => {
            const existingItem = state.cart.find(item => item.id === product.id);
            if (existingItem) {
              return {
                cart: state.cart.map(item =>
                  item.id === product.id
                    ? { ...item, quantity: item.quantity + quantity, personalizationDetails }
                    : item
                )
              };
            } else {
              return {
                cart: [...state.cart, { ...product, quantity, personalizationDetails }]
              };
            }
          });
        }
      },
      removeFromCart: async (productId) => {
        const { currentUser } = get();
        if (currentUser) {
          try {
            // Find the cart item to get its backend ID
            const cartItem = get().cart.find(item => item.id === productId);
            if (cartItem && 'backendId' in cartItem) {
              await apiClient.removeFromCart((cartItem as any).backendId);
            }
            await get().syncCartWithBackend();
          } catch (error) {
            console.error('Failed to remove from cart:', error);
            // Fallback to local removal
            set((state) => ({
              cart: state.cart.filter(item => item.id !== productId)
            }));
          }
        } else {
          set((state) => ({
            cart: state.cart.filter(item => item.id !== productId)
          }));
        }
      },
      updateCartQuantity: async (productId, quantity) => {
        const { currentUser } = get();
        if (currentUser) {
          try {
            const cartItem = get().cart.find(item => item.id === productId);
            if (cartItem && 'backendId' in cartItem) {
              await apiClient.updateCartItem((cartItem as any).backendId, quantity);
            }
            await get().syncCartWithBackend();
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
        if (currentUser) {
          try {
            await apiClient.clearCart();
          } catch (error) {
            console.error('Failed to clear cart:', error);
          }
        }
        set({ cart: [] });
      },
      getCartTotal: () => {
        const { cart } = get();
        return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
      },
      syncCartWithBackend: async () => {
        const { currentUser } = get();
        if (!currentUser) return;
        
        try {
          const backendCart = await apiClient.getCartItems();
          // Convert backend cart items to frontend format
          const frontendCart: CartItem[] = backendCart.map((item: any) => ({
            id: item.productId.toString(),
            name: item.productName,
            description: item.productDescription || '',
            price: Number(item.productPrice),
            quantity: item.quantity,
            image: item.imageUrl || '/placeholder.svg',
            category: 'Unknown',
            size: 'Medium',
            stock: 999,
            rating: 0,
            reviews: 0,
            backendId: item.itemId || item.id,
            personalizationDetails: item.personalizationDetails
          }));
          set({ cart: frontendCart });
        } catch (error) {
          console.error('Failed to sync cart with backend:', error);
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
      checkout: async (customerName: string, customerEmail: string, deliveryAddress: string, contactNumber: string, paymentMethod: string) => {
        try {
          const { currentUser, cart } = get();
          
          // For guest users, send cart items in the request
          const cartItems = currentUser ? undefined : cart.map(item => ({
            productId: parseInt(item.id),
            productName: item.name,
            productPrice: item.price,
            quantity: item.quantity,
            personalizationDetails: item.personalizationDetails,
            customizationId: item.customizationId
          }));

          const result = await apiClient.checkout({
            customerName,
            customerEmail,
            deliveryAddress,
            contactNumber,
            paymentMethod,
            cartItems
          });
          // Clear cart after successful checkout
          await get().clearCart();
          return result;
        } catch (error) {
          console.error('Checkout failed:', error);
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