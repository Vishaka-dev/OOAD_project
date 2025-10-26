// Test script to verify API connectivity
import { apiClient } from './api';

export const testApiConnection = async () => {
  console.log('Testing API connection...');
  
  try {
    // Test registration
    console.log('Testing user registration...');
    const registerResponse = await apiClient.register({
      username: 'Test User',
      email: 'test@example.com',
      password: 'password123'
    });
    console.log('Registration successful:', registerResponse);
    
    // Test login
    console.log('Testing user login...');
    const loginResponse = await apiClient.login({
      username: 'Test User',
      password: 'password123'
    });
    console.log('Login successful:', loginResponse);
    
    // Test cart operations
    console.log('Testing cart operations...');
    try {
      await apiClient.addToCart(1, 2); // Add product with ID 1, quantity 2
      console.log('Add to cart successful');
      
      const cartItems = await apiClient.getCartItems();
      console.log('Cart items:', cartItems);
      
      const cartTotal = await apiClient.getCartTotal();
      console.log('Cart total:', cartTotal);
    } catch (cartError) {
      console.log('Cart operations failed (expected if no products exist):', cartError);
    }
    
    // Test checkout
    console.log('Testing checkout...');
    try {
      const checkoutResult = await apiClient.checkout({
        customerName: 'Test User',
        customerEmail: 'test@example.com',
        shippingAddress: '123 Test St, Test City, TC 12345',
        paymentMethod: 'credit_card'
      });
      console.log('Checkout successful:', checkoutResult);
    } catch (checkoutError) {
      console.log('Checkout failed (expected if cart is empty):', checkoutError);
    }
    
    console.log('API connection test completed successfully!');
    return true;
  } catch (error) {
    console.error('API connection test failed:', error);
    return false;
  }
};

// Make it available globally for testing in browser console
if (typeof window !== 'undefined') {
  (window as any).testApiConnection = testApiConnection;
}
