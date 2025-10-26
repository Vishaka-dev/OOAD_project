import { Product, Category, UIProduct } from '@/types/product';

/**
 * Convert backend Product to frontend UIProduct format
 */
export function convertProductToUI(backendProduct: Product): UIProduct {
  return {
    id: backendProduct.productId.toString(),
    name: backendProduct.name,
    description: backendProduct.description,
    price: backendProduct.price,
    image: backendProduct.imageUrl || '/placeholder.svg',
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

/**
 * Generate mock UI products for development/testing
 */
export function generateMockProducts(): UIProduct[] {
  return [
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
}

/**
 * Generate mock categories for development/testing
 */
export function generateMockCategories() {
  return [
    {
      id: '1',
      name: 'Classic',
      description: 'Traditional teddy bears with timeless appeal',
      productCount: 12
    },
    {
      id: '2',
      name: 'Princess',
      description: 'Elegant and sparkly bears for little princesses',
      productCount: 8
    },
    {
      id: '3',
      name: 'Mini',
      description: 'Small and portable bears for on-the-go',
      productCount: 15
    },
    {
      id: '4',
      name: 'Giant',
      description: 'Extra large bears for maximum cuddles',
      productCount: 5
    },
    {
      id: '5',
      name: 'Scented',
      description: 'Bears with delightful fragrances',
      productCount: 10
    },
    {
      id: '6',
      name: 'Adventure',
      description: 'Bears ready for outdoor adventures',
      productCount: 7
    }
  ];
}
