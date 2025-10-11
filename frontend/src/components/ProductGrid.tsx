import { useState } from "react";
import { Filter, Grid3X3, List } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ProductCard } from "./ProductCard";
import { useStore } from "@/hooks/useStore";
import { UIProduct } from "@/types/product";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface ProductGridProps {
  products?: UIProduct[];
  viewMode?: "grid" | "list";
  showFilters?: boolean;
  showHeader?: boolean;
  showCategories?: boolean;
}

export function ProductGrid({
  products: propProducts,
  viewMode: propViewMode = "grid",
  showFilters = true,
  showHeader = true,
  showCategories = true,
}: ProductGridProps) {
  const { products: storeProducts } = useStore();
  const products = propProducts || storeProducts;

  console.log("🔄 ProductGrid rendered with products:", {
    propProducts: propProducts?.length || 0,
    storeProducts: storeProducts?.length || 0,
    finalProducts: products?.length || 0,
    products: products,
  });

  const [selectedCategory, setSelectedCategory] = useState<string>("all");
  const [selectedSize, setSelectedSize] = useState<string>("all");
  const [sortBy, setSortBy] = useState<string>("name");
  const [viewMode, setViewMode] = useState<"grid" | "list">(propViewMode);

  // Get unique categories and sizes
  const categories = [
    "all",
    ...Array.from(new Set(products.map((p) => p.category))),
  ];
  const sizes = ["all", ...Array.from(new Set(products.map((p) => p.size)))];

  // Filter and sort products
  let filteredProducts = products.filter((product) => {
    const categoryMatch =
      selectedCategory === "all" || product.category === selectedCategory;
    const sizeMatch = selectedSize === "all" || product.size === selectedSize;
    return categoryMatch && sizeMatch;
  });

  // Sort products
  filteredProducts = filteredProducts.sort((a, b) => {
    switch (sortBy) {
      case "price-low":
        return a.price - b.price;
      case "price-high":
        return b.price - a.price;
      case "rating":
        return b.rating - a.rating;
      case "name":
      default:
        return a.name.localeCompare(b.name);
    }
  });

  return (
    <section className="py-16 bg-background" id="products">
      <div className="container max-w-screen-2xl">
        {/* Header */}
        {showHeader && (
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">
              <span className="bg-gradient-to-r from-teddy-700 to-pink-600 bg-clip-text text-transparent">
                Our Collection
              </span>
            </h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Discover our handpicked selection of premium teddy bears and plush
              toys, each crafted with love and attention to detail.
            </p>
          </div>
        )}

        {/* Filters */}
        {showFilters && (
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 mb-8 p-4 bg-muted/30 rounded-lg">
            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center space-x-2">
                <Filter className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm font-medium">Filters:</span>
              </div>

              <Select
                value={selectedCategory}
                onValueChange={setSelectedCategory}
              >
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Category" />
                </SelectTrigger>
                <SelectContent className="bg-popover border border-border z-50">
                  {categories.map((category) => (
                    <SelectItem key={category} value={category}>
                      {category === "all" ? "All Categories" : category}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Select value={selectedSize} onValueChange={setSelectedSize}>
                <SelectTrigger className="w-32">
                  <SelectValue placeholder="Size" />
                </SelectTrigger>
                <SelectContent className="bg-popover border border-border z-50">
                  {sizes.map((size) => (
                    <SelectItem key={size} value={size}>
                      {size === "all" ? "All Sizes" : size}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Select value={sortBy} onValueChange={setSortBy}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Sort by" />
                </SelectTrigger>
                <SelectContent className="bg-popover border border-border z-50">
                  <SelectItem value="name">Name A-Z</SelectItem>
                  <SelectItem value="price-low">Price Low-High</SelectItem>
                  <SelectItem value="price-high">Price High-Low</SelectItem>
                  <SelectItem value="rating">Highest Rated</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-center space-x-2">
              <Badge variant="outline">
                {filteredProducts.length} products
              </Badge>
              <div className="flex items-center border rounded-md">
                <Button
                  variant={viewMode === "grid" ? "default" : "ghost"}
                  size="sm"
                  onClick={() => setViewMode("grid")}
                  className="rounded-r-none"
                >
                  <Grid3X3 className="h-4 w-4" />
                </Button>
                <Button
                  variant={viewMode === "list" ? "default" : "ghost"}
                  size="sm"
                  onClick={() => setViewMode("list")}
                  className="rounded-l-none"
                >
                  <List className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* Products Grid */}
        {filteredProducts.length === 0 ? (
          <div className="text-center py-16">
            <div className="text-6xl mb-4">🧸</div>
            <h3 className="text-xl font-semibold mb-2">No products found</h3>
            <p className="text-muted-foreground">
              Try adjusting your filters to see more products.
            </p>
          </div>
        ) : (
          <div
            className={`grid gap-6 ${
              viewMode === "grid"
                ? "grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-3 2xl:grid-cols-3"
                : "grid-cols-1"
            }`}
          >
            {filteredProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onViewDetails={(product) => {
                  // In a real app, this would navigate to product detail page
                  console.log("View details for:", product.name);
                }}
              />
            ))}
          </div>
        )}

        {/* Featured Categories */}
        {showCategories && (
          <div className="mt-16" id="categories">
            <h3 className="text-2xl font-bold text-center mb-8">
              Shop by Category
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {categories.slice(1).map((category) => (
                <Button
                  key={category}
                  variant="soft"
                  className="h-24 flex-col space-y-2"
                  onClick={() => setSelectedCategory(category)}
                >
                  <span className="text-2xl">
                    {category === "Classic"
                      ? "🧸"
                      : category === "Princess"
                      ? "👑"
                      : category === "Mini"
                      ? "🤏"
                      : category === "Giant"
                      ? "🦣"
                      : category === "Scented"
                      ? "🌸"
                      : category === "Adventure"
                      ? "🎒"
                      : "🧸"}
                  </span>
                  <span className="text-sm font-medium">{category}</span>
                </Button>
              ))}
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
