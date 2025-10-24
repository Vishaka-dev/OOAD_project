import { Product, Category, UIProduct } from '@/types/product';

const API_BASE_URL = 'https://backend-production-8f5c.up.railway.app';

/**
 * Convert backend Product to frontend UIProduct format
 */
export function convertProductToUI(backendProduct: Product): UIProduct {
  // If imageUrl starts with /uploads, prepend the backend URL
  let imageUrl = backendProduct.imageUrl || '/placeholder.svg';
  if (imageUrl.startsWith('/uploads')) {
    imageUrl = `${API_BASE_URL}${imageUrl}`;
  }
  
  return {
    id: backendProduct.productId.toString(),
    name: backendProduct.name,
    description: backendProduct.description,
    price: backendProduct.price,
    image: imageUrl,
    category: backendProduct.categoryName,
    size: 'Medium', // Default size since backend doesn't have this field
    stock: backendProduct.stockQuantity,
    featured: false, // Default to false, can be enhanced later
    rating: 4.5, // Default rating, can be enhanced later
    reviews: Math.floor(Math.random() * 100) // Mock reviews count
  };
}

/**
 * Convert array of backend Products to frontend UIProducts
 */
export function convertProductsToUI(backendProducts: Product[]): UIProduct[] {
  return backendProducts.map(convertProductToUI);
}

/**
 * Convert backend Category to a more UI-friendly format
 */
export function convertCategoryToUI(backendCategory: Category) {
  return {
    id: backendCategory.categoryId.toString(),
    name: backendCategory.name,
    description: backendCategory.description,
    productCount: backendCategory.productCount
  };
}

/**
 * Convert array of backend Categories to UI format
 */
export function convertCategoriesToUI(backendCategories: Category[]) {
  return backendCategories.map(convertCategoryToUI);
}

