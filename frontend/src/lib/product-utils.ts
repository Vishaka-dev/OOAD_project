import { Product, Category, UIProduct } from '@/types/product';

/**
 * Convert a backend Product to UIProduct format
 */
export function convertProductToUI(backendProduct: Product): UIProduct {
  return {
    id: backendProduct.productId.toString(),
    name: backendProduct.name,
    description: backendProduct.description,
    price: backendProduct.price,
    image: backendProduct.imageUrl || '/placeholder.svg',
    category: backendProduct.categoryName,
    size: 'Medium', // Default size since backend doesn't have size
    stock: backendProduct.stockQuantity,
    rating: 4.5, // Default rating
    reviews: Math.floor(Math.random() * 100), // Random reviews count
  };
}

/**
 * Convert an array of backend Products to UIProduct format
 */
export function convertProductsToUI(backendProducts: Product[]): UIProduct[] {
  return backendProducts.map(convertProductToUI);
}

/**
 * Convert backend Categories to UI format
 */
export function convertCategoriesToUI(backendCategories: Category[]): any[] {
  return backendCategories.map(category => ({
    id: category.categoryId.toString(),
    name: category.name,
    description: category.description,
    productCount: category.productCount,
  }));
}

/**
 * Generate mock products for fallback when API is unavailable
 */
export function generateMockProducts(): UIProduct[] {
  return [
    {
      id: '1',
      name: 'Classic Teddy Bear',
      description: 'A soft and cuddly classic teddy bear perfect for any occasion.',
      price: 29.99,
      originalPrice: 39.99,
      image: '/placeholder.svg',
      category: 'Classic Bears',
      size: 'Medium',
      stock: 50,
      featured: true,
      rating: 4.8,
      reviews: 127,
    },
    {
      id: '2',
      name: 'Premium Plush Bear',
      description: 'High-quality plush bear with premium materials and craftsmanship.',
      price: 49.99,
      image: '/placeholder.svg',
      category: 'Premium Bears',
      size: 'Large',
      stock: 25,
      featured: true,
      rating: 4.9,
      reviews: 89,
    },
    {
      id: '3',
      name: 'Mini Teddy Bear',
      description: 'Small and adorable teddy bear, perfect for children.',
      price: 19.99,
      image: '/placeholder.svg',
      category: 'Mini Bears',
      size: 'Small',
      stock: 75,
      featured: false,
      rating: 4.6,
      reviews: 203,
    },
    {
      id: '4',
      name: 'Giant Hug Bear',
      description: 'Extra large teddy bear for the ultimate cuddle experience.',
      price: 79.99,
      image: '/placeholder.svg',
      category: 'Giant Bears',
      size: 'Extra Large',
      stock: 15,
      featured: true,
      rating: 4.7,
      reviews: 45,
    },
    {
      id: '5',
      name: 'Rainbow Teddy Bear',
      description: 'Colorful rainbow teddy bear that brings joy and happiness.',
      price: 34.99,
      image: '/placeholder.svg',
      category: 'Colorful Bears',
      size: 'Medium',
      stock: 40,
      featured: false,
      rating: 4.5,
      reviews: 78,
    },
    {
      id: '6',
      name: 'Vintage Style Bear',
      description: 'Classic vintage-style teddy bear with traditional design.',
      price: 44.99,
      image: '/placeholder.svg',
      category: 'Vintage Bears',
      size: 'Medium',
      stock: 30,
      featured: false,
      rating: 4.4,
      reviews: 56,
    },
    {
      id: '7',
      name: 'Musical Teddy Bear',
      description: 'Soft teddy bear that plays gentle lullabies.',
      price: 39.99,
      image: '/placeholder.svg',
      category: 'Musical Bears',
      size: 'Medium',
      stock: 20,
      featured: true,
      rating: 4.6,
      reviews: 92,
    },
    {
      id: '8',
      name: 'Sports Team Bear',
      description: 'Teddy bear wearing your favorite sports team colors.',
      price: 32.99,
      image: '/placeholder.svg',
      category: 'Sports Bears',
      size: 'Medium',
      stock: 60,
      featured: false,
      rating: 4.3,
      reviews: 34,
    },
  ];
}

/**
 * Generate mock categories for fallback when API is unavailable
 */
export function generateMockCategories(): any[] {
  return [
    {
      id: '1',
      name: 'Classic Bears',
      description: 'Traditional teddy bears with timeless appeal',
      productCount: 12,
    },
    {
      id: '2',
      name: 'Premium Bears',
      description: 'High-quality bears made with premium materials',
      productCount: 8,
    },
    {
      id: '3',
      name: 'Mini Bears',
      description: 'Small and adorable bears perfect for children',
      productCount: 15,
    },
    {
      id: '4',
      name: 'Giant Bears',
      description: 'Extra large bears for the ultimate cuddle experience',
      productCount: 5,
    },
    {
      id: '5',
      name: 'Colorful Bears',
      description: 'Bright and colorful bears that bring joy',
      productCount: 10,
    },
    {
      id: '6',
      name: 'Vintage Bears',
      description: 'Classic vintage-style bears with traditional design',
      productCount: 7,
    },
    {
      id: '7',
      name: 'Musical Bears',
      description: 'Bears that play music and lullabies',
      productCount: 6,
    },
    {
      id: '8',
      name: 'Sports Bears',
      description: 'Bears representing your favorite sports teams',
      productCount: 9,
    },
  ];
}
